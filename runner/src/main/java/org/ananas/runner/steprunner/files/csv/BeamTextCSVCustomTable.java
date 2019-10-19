package org.ananas.runner.steprunner.files.csv;

import org.ananas.runner.core.common.Sampler;
import org.ananas.runner.core.errors.ErrorHandler;
import org.ananas.runner.steprunner.files.txt.TruncatedTextIO;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.Row;
import org.apache.commons.csv.CSVFormat;

/**
 * {@code BeamTextCSVCustomTable} is a {@code BeamTextTable} which formatted in CSV. Unlike {@code
 * BeamTextCSVTable}, this table skips header line due to
 * https://issues.apache.org/jira/browse/BEAM-123
 *
 * <p>
 *
 * <p>{@link CSVFormat} itself has many dialects, check its javadoc for more info.
 */
public class BeamTextCSVCustomTable {

  private static final long serialVersionUID = -5782499552446468380L;
  private boolean isHeader;
  private String filePattern;
  private CSVFormat csvFormat;
  private boolean doSampling;
  private boolean isTest;
  private Schema schema;
  private boolean isCompressed;
  protected ErrorHandler errors;

  public BeamTextCSVCustomTable(
      ErrorHandler errors,
      Schema schema,
      String filePattern,
      CSVFormat csvFormat,
      boolean isHeader,
      boolean doSampling,
      boolean isTest) {
    this.schema = schema;
    this.errors = errors;
    this.filePattern = filePattern;
    this.csvFormat = csvFormat;
    this.doSampling = doSampling;
    this.isTest = isTest;
    this.isHeader = isHeader;
  }

  public PCollection<Row> buildIOReader(Pipeline pipeline) {
    PCollection<String> p =
        PBegin.in(pipeline)
            .apply(
                "decodeRecord",
                this.isTest
                    ? TruncatedTextIO.read().from(this.filePattern)
                    : TextIO.read().from(this.filePattern));
    p = Sampler.sample(p, 1000, (this.doSampling || this.isTest));
    return WithHeader(p)
        .apply(
            "parseCSVLine", new BeamTextCSVTableIOReader(this.schema, this.csvFormat, this.errors));
  }

  private PCollection<String> WithHeader(PCollection<String> collection) {
    if (this.isHeader) {
      return collection.apply(
          "nonHeaderRecord", Filter.by(matchNonHeader(this.schema, this.csvFormat)));
    } else {
      return collection;
    }
  }

  private static SerializableFunction<String, Boolean> matchNonHeader(
      Schema schema, CSVFormat csvFormat) {
    String str =
        String.format(
            "%s%s%s%s",
            csvFormat.getQuoteCharacter(),
            schema.getField(0).getName(),
            csvFormat.getQuoteCharacter(),
            csvFormat.getDelimiter());
    return input -> !input.startsWith(str);
  }
}
