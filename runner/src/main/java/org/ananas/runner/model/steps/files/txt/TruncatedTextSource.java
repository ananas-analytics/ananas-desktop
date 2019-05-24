package org.ananas.runner.model.steps.files.txt;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.NoSuchElementException;
import javax.annotation.Nullable;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.FileBasedSource;
import org.apache.beam.sdk.io.fs.EmptyMatchTreatment;
import org.apache.beam.sdk.io.fs.MatchResult;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.ValueProvider;

/** Text Source whose offset is < endOffset */
public class TruncatedTextSource extends FileBasedSource<String> {
  private static final long serialVersionUID = 432564366238941768L;
  private byte[] delimiter;

  TruncatedTextSource(
      ValueProvider<String> fileSpec, EmptyMatchTreatment emptyMatchTreatment, byte[] delimiter) {
    super(fileSpec, emptyMatchTreatment, 1L);
    this.delimiter = delimiter;
  }

  private TruncatedTextSource(
      MatchResult.Metadata metadata, long start, long end, byte[] delimiter) {
    super(metadata, 1L, start, end);
    this.delimiter = delimiter;
  }

  @Override
  protected FileBasedSource<String> createForSubrangeOfFile(
      MatchResult.Metadata metadata, long start, long end) {
    // hack : We force the offset here to reduce latency for testing purpose
    return new TruncatedTextSource(
        metadata, 0l, Math.min(getEndOffset(), 1000 * 1024l), this.delimiter);
  }

  @Override
  protected boolean isSplittable() throws Exception {
    return false;
  }

  @Override
  protected FileBasedReader<String> createSingleFileReader(PipelineOptions options) {
    return new TruncatedTextSource.TextBasedReader(this, this.delimiter);
  }

  @Override
  public Coder<String> getOutputCoder() {
    return StringUtf8Coder.of();
  }

  /**
   * A {@link FileBasedReader FileBasedReader} which can decode records delimited by delimiter
   * characters.
   *
   * <p>
   *
   * <p>See {@link org.apache.beam.sdk.io.TextSource} for further details.
   */
  @VisibleForTesting
  static class TextBasedReader extends FileBasedReader<String> {
    private static final int READ_BUFFER_SIZE = 8192;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(READ_BUFFER_SIZE);
    private ByteString buffer;
    private int startOfDelimiterInBuffer;
    private int endOfDelimiterInBuffer;
    private long startOfRecord;
    private volatile long startOfNextRecord;
    private volatile boolean eof;
    private volatile boolean elementIsPresent;
    private @Nullable String currentValue;
    private @Nullable ReadableByteChannel inChannel;
    private @Nullable byte[] delimiter;

    private TextBasedReader(TruncatedTextSource source, byte[] delimiter) {
      super(source);
      this.buffer = ByteString.EMPTY;
      this.delimiter = delimiter;
    }

    @Override
    protected long getCurrentOffset() throws NoSuchElementException {
      if (!this.elementIsPresent) {
        throw new NoSuchElementException();
      }
      return this.startOfRecord;
    }

    @Override
    public long getSplitPointsRemaining() {
      if (isStarted() && this.startOfNextRecord >= getCurrentSource().getEndOffset()) {
        return isDone() ? 0 : 1;
      }
      return super.getSplitPointsRemaining();
    }

    @Override
    public String getCurrent() throws NoSuchElementException {
      if (!this.elementIsPresent) {
        throw new NoSuchElementException();
      }
      return this.currentValue;
    }

    @Override
    protected void startReading(ReadableByteChannel channel) throws IOException {
      this.inChannel = channel;
      // If the first offset is greater than zero, we need to skip bytes until we see our
      // first delimiter.
      long startOffset = getCurrentSource().getStartOffset();
      if (startOffset > 0) {
        checkState(
            channel instanceof SeekableByteChannel,
            "%s only supports reading from a SeekableByteChannel when given a start offset"
                + " greater than 0.",
            TruncatedTextSource.class.getSimpleName());
        long requiredPosition = startOffset - 1;
        if (this.delimiter != null && startOffset >= this.delimiter.length) {
          // we need to move back the offset of at worse delimiter.size to be sure to see
          // all the bytes of the delimiter in the call to findDelimiterBounds() below
          requiredPosition = startOffset - this.delimiter.length;
        }
        ((SeekableByteChannel) channel).position(requiredPosition);
        findDelimiterBounds();
        this.buffer = this.buffer.substring(this.endOfDelimiterInBuffer);
        this.startOfNextRecord = requiredPosition + this.endOfDelimiterInBuffer;
        this.endOfDelimiterInBuffer = 0;
        this.startOfDelimiterInBuffer = 0;
      }
    }

    /**
     * Locates the start position and end position of the next delimiter. Will consume the channel
     * till either EOF or the delimiter bounds are found.
     *
     * <p>
     *
     * <p>This fills the buffer and updates the positions as follows:
     *
     * <pre>{@code
     * ------------------------------------------------------
     * | element bytes | delimiter bytes | unconsumed bytes |
     * ------------------------------------------------------
     * 0            start of          end of              buffer
     *              delimiter         delimiter           size
     *              in buffer         in buffer
     * }</pre>
     */
    private void findDelimiterBounds() throws IOException {
      int bytePositionInBuffer = 0;
      while (true) {
        if (!tryToEnsureNumberOfBytesInBuffer(bytePositionInBuffer + 1)) {
          this.startOfDelimiterInBuffer = this.endOfDelimiterInBuffer = bytePositionInBuffer;
          break;
        }

        byte currentByte = this.buffer.byteAt(bytePositionInBuffer);

        if (this.delimiter == null) {
          // default delimiter
          if (currentByte == '\n') {
            this.startOfDelimiterInBuffer = bytePositionInBuffer;
            this.endOfDelimiterInBuffer = this.startOfDelimiterInBuffer + 1;
            break;
          } else if (currentByte == '\r') {
            this.startOfDelimiterInBuffer = bytePositionInBuffer;
            this.endOfDelimiterInBuffer = this.startOfDelimiterInBuffer + 1;

            if (tryToEnsureNumberOfBytesInBuffer(bytePositionInBuffer + 2)) {
              currentByte = this.buffer.byteAt(bytePositionInBuffer + 1);
              if (currentByte == '\n') {
                this.endOfDelimiterInBuffer += 1;
              }
            }
            break;
          }
        } else {
          // user defined delimiter
          int i = 0;
          // initialize delimiter not found
          this.startOfDelimiterInBuffer = this.endOfDelimiterInBuffer = bytePositionInBuffer;
          while ((i <= this.delimiter.length - 1) && (currentByte == this.delimiter[i])) {
            // read next byte
            i++;
            if (tryToEnsureNumberOfBytesInBuffer(bytePositionInBuffer + i + 1)) {
              currentByte = this.buffer.byteAt(bytePositionInBuffer + i);
            } else {
              // corner case: delimiter truncated at the end of the file
              this.startOfDelimiterInBuffer = this.endOfDelimiterInBuffer = bytePositionInBuffer;
              break;
            }
          }
          if (i == this.delimiter.length) {
            // all bytes of delimiter found
            this.endOfDelimiterInBuffer = bytePositionInBuffer + i;
            break;
          }
        }
        // Move to the next byte in buffer.
        bytePositionInBuffer += 1;
      }
    }

    @Override
    protected boolean readNextRecord() throws IOException {
      this.startOfRecord = this.startOfNextRecord;
      findDelimiterBounds();

      // If we have reached EOF file and consumed all of the buffer then we know
      // that there are no more records.
      if (this.eof && this.buffer.isEmpty()) {
        this.elementIsPresent = false;
        return false;
      }

      decodeCurrentElement();
      this.startOfNextRecord = this.startOfRecord + this.endOfDelimiterInBuffer;
      return true;
    }

    /**
     * Decodes the current element updating the buffer to only contain the unconsumed bytes.
     *
     * <p>
     *
     * <p>This invalidates the currently stored {@code startOfDelimiterInBuffer} and {@code
     * endOfDelimiterInBuffer}.
     */
    private void decodeCurrentElement() throws IOException {
      ByteString dataToDecode = this.buffer.substring(0, this.startOfDelimiterInBuffer);
      this.currentValue = dataToDecode.toStringUtf8();
      this.elementIsPresent = true;
      this.buffer = this.buffer.substring(this.endOfDelimiterInBuffer);
    }

    /** Returns false if we were unable to ensure the minimum capacity by consuming the channel. */
    private boolean tryToEnsureNumberOfBytesInBuffer(int minCapacity) throws IOException {
      // While we aren't at EOF or haven't fulfilled the minimum buffer capacity,
      // attempt to read more bytes.
      while (this.buffer.size() <= minCapacity && !this.eof) {
        this.eof = this.inChannel.read(this.readBuffer) == -1;
        this.readBuffer.flip();
        this.buffer = this.buffer.concat(ByteString.copyFrom(this.readBuffer));
        this.readBuffer.clear();
      }
      // Return true if we were able to honor the minimum buffer capacity request
      return this.buffer.size() >= minCapacity;
    }
  }
}
