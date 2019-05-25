package org.ananas.runner.steprunner.files.txt;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.auto.value.AutoValue;
import java.util.Arrays;
import javax.annotation.Nullable;
import org.apache.beam.sdk.io.Compression;
import org.apache.beam.sdk.io.FileBasedSource;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.io.fs.EmptyMatchTreatment;
import org.apache.beam.sdk.options.ValueProvider;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.display.DisplayData;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;

public class TruncatedTextIO {
  /**
   * A {@link PTransform} that reads from one or more text files and returns a bounded {@link
   * PCollection} containing one element for each line of the input files.
   */
  public static TruncatedTextIO.Read read() {
    return new AutoValue_TruncatedTextIO_Read.Builder()
        .setCompression(Compression.AUTO)
        .setHintMatchesManyFiles(false)
        .setMatchConfiguration(FileIO.MatchConfiguration.create(EmptyMatchTreatment.DISALLOW))
        .build();
  }

  /** Implementation of {@link #read}. */
  @AutoValue
  public abstract static class Read extends PTransform<PBegin, PCollection<String>> {
    private static final long serialVersionUID = 4024143658426874147L;

    @Nullable
    abstract ValueProvider<String> getFilepattern();

    abstract Compression getCompression();

    abstract FileIO.MatchConfiguration getMatchConfiguration();

    abstract boolean getHintMatchesManyFiles();

    // this returns an array that can be mutated by the caller
    @Nullable
    @SuppressWarnings("mutable")
    abstract byte[] getDelimiter();

    abstract TruncatedTextIO.Read.Builder toBuilder();

    @AutoValue.Builder
    abstract static class Builder {
      abstract TruncatedTextIO.Read.Builder setFilepattern(ValueProvider<String> filepattern);

      abstract TruncatedTextIO.Read.Builder setCompression(Compression compression);

      abstract TruncatedTextIO.Read.Builder setDelimiter(byte[] delimiter);

      abstract TruncatedTextIO.Read.Builder setMatchConfiguration(
          FileIO.MatchConfiguration matchConfiguration);

      abstract TruncatedTextIO.Read.Builder setHintMatchesManyFiles(boolean hintManyFiles);

      abstract TruncatedTextIO.Read build();
    }

    /**
     * Reads text files that reads from the file(s) with the given filename or filename pattern.
     *
     * <p>
     *
     * <p>This can be a local path (if running locally), or a Google Cloud Storage filename or
     * filename pattern of the form {@code "gs://<bucket>/<filepath>"} (if running locally or using
     * remote execution service).
     *
     * <p>
     *
     * <p>Standard <a href="http://docs.oracle.com/javase/tutorial/essential/io/find.html" >Java
     * Filesystem glob patterns</a> ("*", "?", "[..]") are supported.
     */
    public TruncatedTextIO.Read from(String filepattern) {
      checkArgument(filepattern != null, "filepattern can not be null");
      return from(ValueProvider.StaticValueProvider.of(filepattern));
    }

    /** Same as {@code from(filepattern)}, but accepting a {@link ValueProvider}. */
    public TruncatedTextIO.Read from(ValueProvider<String> filepattern) {
      checkArgument(filepattern != null, "filepattern can not be null");
      return toBuilder().setFilepattern(filepattern).build();
    }

    @Override
    public PCollection<String> expand(PBegin input) {
      checkNotNull(
          getFilepattern(), "need to set the filepattern of a TruncatedTextIO.Read transform");
      return input.apply("Read", org.apache.beam.sdk.io.Read.from(getSource()));
    }

    // Helper to create a source specific to the requested compression type.
    protected FileBasedSource<String> getSource() {
      return TruncatedCompressedSource.from(
          new TruncatedTextSource(getFilepattern(), EmptyMatchTreatment.DISALLOW, getDelimiter()));
    }

    @Override
    public void populateDisplayData(DisplayData.Builder builder) {
      super.populateDisplayData(builder);
      builder
          .add(
              DisplayData.item("compressionType", getCompression().toString())
                  .withLabel("Compression Type"))
          .addIfNotNull(DisplayData.item("filePattern", getFilepattern()).withLabel("File Pattern"))
          .include("matchConfiguration", getMatchConfiguration())
          .addIfNotNull(
              DisplayData.item("delimiter", Arrays.toString(getDelimiter()))
                  .withLabel("Custom delimiter to split records"));
    }
  }

  /** @deprecated Use {@link Compression}. */
  @Deprecated
  public enum CompressionType {
    /** @see Compression#AUTO */
    AUTO(Compression.AUTO),

    /** @see Compression#UNCOMPRESSED */
    UNCOMPRESSED(Compression.UNCOMPRESSED),

    /** @see Compression#GZIP */
    GZIP(Compression.GZIP),

    /** @see Compression#BZIP2 */
    BZIP2(Compression.BZIP2),

    /** @see Compression#ZIP */
    ZIP(Compression.ZIP),

    /** @see Compression#ZIP */
    DEFLATE(Compression.DEFLATE);

    private final Compression canonical;

    CompressionType(Compression canonical) {
      this.canonical = canonical;
    }

    /** @see Compression#matches */
    public boolean matches(String filename) {
      return this.canonical.matches(filename);
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  /** Disable construction of utility class. */
  private TruncatedTextIO() {}
}
