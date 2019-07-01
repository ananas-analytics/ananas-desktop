package org.ananas.runner.steprunner.files.txt;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.NoSuchElementException;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import org.apache.beam.sdk.annotations.Experimental;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.io.CompressedSource;
import org.apache.beam.sdk.io.Compression;
import org.apache.beam.sdk.io.FileBasedSource;
import org.apache.beam.sdk.io.fs.MatchResult.Metadata;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.display.DisplayData;
import org.joda.time.Instant;

/**
 * A Source that reads from compressed files. A {@code CompressedSources} wraps a delegate {@link
 * FileBasedSource} that is able to read the decompressed file format.
 *
 * <p>
 *
 * <p>For example, use the following to read from a gzip-compressed file-based source:
 *
 * <p>
 *
 * <pre>{@code
 * FileBasedSource<T> mySource = ...;
 * PCollection<T> collection = p.apply(Read.from(CompressedSource
 *     .from(mySource)
 *     .withCompression(Compression.GZIP)));
 * }</pre>
 *
 * <p>
 *
 * <p>Supported compression algorithms are {@link Compression#GZIP}, {@link Compression#BZIP2},
 * {@link Compression#ZIP} and {@link Compression#DEFLATE}. User-defined compression types are
 * supported by implementing a {@link DecompressingChannelFactory}.
 *
 * <p>
 *
 * <p>By default, the compression algorithm is selected from those supported in {@link Compression}
 * based on the file name provided to the source, namely {@code ".bz2"} indicates {@link
 * Compression#BZIP2}, {@code ".gz"} indicates {@link Compression#GZIP}, {@code ".zip"} indicates
 * {@link Compression#ZIP} and {@code ".deflate"} indicates {@link Compression#DEFLATE}. If the file
 * name does not match any of the supported algorithms, it is assumed to be uncompressed data.
 *
 * @param <T> The type to read from the compressed file.
 */
@Experimental(Experimental.Kind.SOURCE_SINK)
public class TruncatedCompressedSource extends FileBasedSource<String> {

  private static final long serialVersionUID = -3171933003316474543L;
  private static int MAX_RECORDS = 50;

  /**
   * Factory interface for creating channels that decompress the content of an underlying channel.
   */
  public interface DecompressingChannelFactory extends Serializable {
    /** Given a channel, create a channel that decompresses the content read from the channel. */
    ReadableByteChannel createDecompressingChannel(ReadableByteChannel channel) throws IOException;
  }

  /** @deprecated Use {@link Compression} instead */
  @Deprecated
  public enum CompressionMode implements DecompressingChannelFactory {
    /** @see Compression#UNCOMPRESSED */
    UNCOMPRESSED(Compression.UNCOMPRESSED),

    /** @see Compression#AUTO */
    AUTO(Compression.AUTO),

    /** @see Compression#GZIP */
    GZIP(Compression.GZIP),

    /** @see Compression#BZIP2 */
    BZIP2(Compression.BZIP2),

    /** @see Compression#ZIP */
    ZIP(Compression.ZIP),

    /** @see Compression#DEFLATE */
    DEFLATE(Compression.DEFLATE);

    private final Compression canonical;

    CompressionMode(Compression canonical) {
      this.canonical = canonical;
    }

    /**
     * Returns {@code true} if the given file name implies that the contents are compressed
     * according to the compression embodied by this factory.
     */
    public boolean matches(String fileName) {
      return this.canonical.matches(fileName);
    }

    @Override
    public ReadableByteChannel createDecompressingChannel(ReadableByteChannel channel)
        throws IOException {
      return this.canonical.readDecompressed(channel);
    }

    /** Returns whether the file's extension matches of one of the known compression formats. */
    public static boolean isCompressed(String filename) {
      return Compression.AUTO.isCompressed(filename);
    }

    static DecompressingChannelFactory fromCanonical(Compression compression) {
      switch (compression) {
        case AUTO:
          return AUTO;

        case UNCOMPRESSED:
          return UNCOMPRESSED;

        case GZIP:
          return GZIP;

        case BZIP2:
          return BZIP2;

        case ZIP:
          return ZIP;

        case DEFLATE:
          return DEFLATE;

        default:
          throw new IllegalArgumentException("Unsupported compression type: " + compression);
      }
    }
  }

  private final TruncatedTextSource sourceDelegate;
  private final DecompressingChannelFactory channelFactory;

  /**
   * Creates a {@code CompressedSource} from an underlying {@code FileBasedSource}. The type of
   * compression used will be based on the file name extension unless explicitly configured via
   * {@link CompressedSource#withDecompression}.
   */
  public static <T> TruncatedCompressedSource from(TruncatedTextSource sourceDelegate) {
    return new TruncatedCompressedSource(sourceDelegate, CompressionMode.AUTO);
  }

  /**
   * Return a {@code CompressedSource} that is like this one but will decompress its underlying file
   * with the given {@link DecompressingChannelFactory}.
   */
  private TruncatedCompressedSource withDecompression(DecompressingChannelFactory channelFactory) {
    return new TruncatedCompressedSource(this.sourceDelegate, channelFactory);
  }

  /** Like {@link #withDecompression} but takes a canonical {@link Compression}. */
  public TruncatedCompressedSource withCompression(Compression compression) {
    return withDecompression(CompressionMode.fromCanonical(compression));
  }

  /**
   * Creates a {@code CompressedSource} from a delegate file based source and a decompressing
   * channel factory.
   */
  private TruncatedCompressedSource(
      TruncatedTextSource sourceDelegate, DecompressingChannelFactory channelFactory) {
    super(sourceDelegate.getFileOrPatternSpecProvider(), Long.MAX_VALUE);
    this.sourceDelegate = sourceDelegate;
    this.channelFactory = channelFactory;
  }

  /**
   * Creates a {@code CompressedSource} for an individual file. Used by {@link
   * CompressedSource#createForSubrangeOfFile}.
   */
  private TruncatedCompressedSource(
      TruncatedTextSource sourceDelegate,
      DecompressingChannelFactory channelFactory,
      Metadata metadata,
      long minBundleSize,
      long startOffset,
      long endOffset) {
    super(metadata, minBundleSize, startOffset, endOffset);
    this.sourceDelegate = sourceDelegate;
    this.channelFactory = channelFactory;
    boolean splittable;
    try {
      splittable = isSplittable();
    } catch (Exception e) {
      throw new RuntimeException("Failed to determine if the source is splittable", e);
    }
    checkArgument(
        splittable || startOffset == 0,
        "CompressedSources must start reading at offset 0. Requested offset: %s",
        startOffset);
  }

  /**
   * Validates that the delegate source is a valid source and that the channel factory is not null.
   */
  @Override
  public void validate() {
    super.validate();
    checkNotNull(this.sourceDelegate);
    this.sourceDelegate.validate();
    checkNotNull(this.channelFactory);
  }

  /**
   * Creates a {@code CompressedSource} for a subrange of a file. Called by superclass to create a
   * source for a single file.
   */
  @Override
  protected FileBasedSource<String> createForSubrangeOfFile(
      Metadata metadata, long start, long end) {
    return new TruncatedCompressedSource(
        (TruncatedTextSource)
            this.sourceDelegate.createForSubrangeOfFile(metadata, start, 1000 * 1024),
        this.channelFactory,
        metadata,
        this.sourceDelegate.getMinBundleSize(),
        start,
        1000 * 1024);
  }

  /**
   * Determines whether a single file represented by this source is splittable. Returns true if we
   * are using the default decompression factory and and it determines from the requested file name
   * that the file is not compressed.
   */
  @Override
  protected final boolean isSplittable() {
    return false;
  }

  /**
   * Creates a {@code FileBasedReader} to read a single file.
   *
   * <p>
   *
   * <p>Uses the delegate source to create a single file reader for the delegate source. Utilizes
   * the default decompression channel factory to not wrap the source reader if the file name does
   * not represent a compressed file allowing for splitting of the source.
   */
  @Override
  protected final FileBasedReader<String> createSingleFileReader(PipelineOptions options) {
    return new CompressedReader(
        this,
        (TruncatedTextSource.TextBasedReader) this.sourceDelegate.createSingleFileReader(options));
  }

  @Override
  public void populateDisplayData(DisplayData.Builder builder) {
    // We explicitly do not register base-class data, instead we use the delegate inner source.
    builder
        .include("source", this.sourceDelegate)
        .add(DisplayData.item("source", this.sourceDelegate.getClass()).withLabel("Read Source"));

    if (this.channelFactory instanceof Enum) {
      // GZIP, BZIP, ZIP and DEFLATE are implemented as enums; Enum classes are anonymous, so use
      // the .name() value instead
      builder.add(
          DisplayData.item("compressionMode", ((Enum) this.channelFactory).name())
              .withLabel("Compression Mode"));
    } else {
      builder.add(
          DisplayData.item("compressionMode", this.channelFactory.getClass())
              .withLabel("Compression Mode"));
    }
  }

  /** Returns the delegate source's output coder. */
  @Override
  public final Coder getOutputCoder() {
    return this.sourceDelegate.getOutputCoder();
  }

  final DecompressingChannelFactory getChannelFactory() {
    return this.channelFactory;
  }

  /**
   * Reader for a {@link CompressedSource}. Decompresses its input and uses a delegate reader to
   * read elements from the decompressed input.
   *
   * @param <T> The type of records read from the source.
   */
  public static class CompressedReader extends FileBasedReader<String> {

    private final TruncatedTextSource.TextBasedReader readerDelegate;
    private final Object progressLock = new Object();

    @GuardedBy("progressLock")
    private long numRecordsRead;

    @Nullable // Initialized in startReading
    @GuardedBy("progressLock")
    private CountingChannel channel;

    private DecompressingChannelFactory channelFactory;

    /** Create a {@code CompressedReader} from a {@code CompressedSource} and delegate reader. */
    CompressedReader(
        TruncatedCompressedSource source, TruncatedTextSource.TextBasedReader readerDelegate) {
      super((FileBasedSource) source);
      this.channelFactory = source.getChannelFactory();
      this.readerDelegate = readerDelegate;
    }

    /** Gets the current record from the delegate reader. */
    @Override
    public String getCurrent() throws NoSuchElementException {
      return this.readerDelegate.getCurrent();
    }

    @Override
    public boolean allowsDynamicSplitting() {
      return false;
    }

    @Override
    public final long getSplitPointsConsumed() {
      synchronized (this.progressLock) {
        return (isDone() && this.numRecordsRead > 0) ? 1 : 0;
      }
    }

    @Override
    public final long getSplitPointsRemaining() {
      return isDone() ? 0 : 1;
    }

    /** Returns true only for the first record; compressed sources cannot be split. */
    @Override
    protected final boolean isAtSplitPoint() {
      // We have to return true for the first record, but not for the state before reading it,
      // and not for the state after reading any other record. Hence == rather than >= or <=.
      // This is required because FileBasedReader is intended for readers that can read a range
      // of offsets in a file and where the range can be split in parts. CompressedReader,
      // however, is a degenerate case because it cannot be split, but it has to satisfy the
      // semantics of offsets and split points anyway.
      synchronized (this.progressLock) {
        return this.numRecordsRead == 1;
      }
    }

    private static class CountingChannel implements ReadableByteChannel {
      long count;
      private final ReadableByteChannel inner;

      CountingChannel(ReadableByteChannel inner, long count) {
        this.inner = inner;
        this.count = count;
      }

      long getCount() {
        return this.count;
      }

      @Override
      public int read(ByteBuffer dst) throws IOException {
        int bytes = this.inner.read(dst);
        if (bytes > 0) {
          // Avoid the -1 from EOF.
          this.count += bytes;
        }
        return bytes;
      }

      @Override
      public boolean isOpen() {
        return this.inner.isOpen();
      }

      @Override
      public void close() throws IOException {
        this.inner.close();
      }
    }

    /**
     * Creates a decompressing channel from the input channel and passes it to its delegate reader's
     * {@link FileBasedReader#startReading(ReadableByteChannel)}.
     */
    @Override
    protected final void startReading(ReadableByteChannel channel) throws IOException {
      synchronized (this.progressLock) {
        this.channel = new CountingChannel(channel, getCurrentSource().getStartOffset());
        channel = this.channel;
      }

      if (this.channelFactory == CompressionMode.AUTO) {
        this.readerDelegate.startReading(
            Compression.detect(getCurrentSource().getFileOrPatternSpec())
                .readDecompressed(channel));
      } else {
        this.readerDelegate.startReading(this.channelFactory.createDecompressingChannel(channel));
      }
    }

    /** Reads the next record via the delegate reader. */
    @Override
    protected final boolean readNextRecord() throws IOException {
      if (!this.readerDelegate.readNextRecord()) {
        return false;
      }

      // Hack
      if (this.numRecordsRead >= MAX_RECORDS) {
        return false;
      }
      synchronized (this.progressLock) {
        ++this.numRecordsRead;
      }
      return true;
    }

    // Unsplittable: returns the offset in the input stream that has been read by the input.
    // these positions are likely to be coarse-grained (in the event of buffering) and
    // over-estimates (because they reflect the number of bytes read to produce an element, not its
    // start) but both of these provide better data than e.g., reporting the start of the file.
    @Override
    protected final long getCurrentOffset() throws NoSuchElementException {
      synchronized (this.progressLock) {
        if (this.numRecordsRead <= 1) {
          // Since the first record is at a split point, it should start at the beginning of the
          // file. This avoids the bad case where the decompressor read the entire file, which
          // would cause the file to be treated as empty when returning channel.getCount() as it
          // is outside the valid range.
          return 0;
        }
        return this.channel.getCount();
      }
    }

    @Override
    public Instant getCurrentTimestamp() throws NoSuchElementException {
      return this.readerDelegate.getCurrentTimestamp();
    }
  }
}
