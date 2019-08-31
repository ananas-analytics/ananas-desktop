package org.ananas.runner.steprunner.gcs;

import org.ananas.runner.kernel.LoaderStepRunner;
import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.misc.AsJsons;
import org.ananas.runner.misc.StepConfigHelper;
import org.ananas.runner.steprunner.files.FileLoader.CSVFileSink;
import org.ananas.runner.steprunner.files.FileLoader.TextFileSink;
import org.ananas.runner.steprunner.files.utils.StepFileConfigToUrl;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.values.Row;

public class GCSLoader extends LoaderStepRunner {

  private static final long serialVersionUID = 1795913085520164620L;
  private String url;
  private String prefix;

  public GCSLoader(Step step, StepRunner previous, boolean isTest) {
    super(step, previous, isTest);
  }

  @Override
  public void build() {
    prefix = StepConfigHelper.getConfig(step.config, "prefix", "output");

    this.output = previous.getOutput();

    if (isTest) {
      // TODO: check configuration here
      return;
    }
    String format = (String) step.config.getOrDefault("format", "");
    switch (format) {
      case "csv":
        buildCsvLoader();
        break;
      case "json":
        buildJsonLoader();
      default:
        break;
    }
  }

  private void buildCsvLoader() {
    url = StepFileConfigToUrl.gcsSourceUrl(step.config);
    this.output.apply(
        FileIO.<Row>write()
            .via(new CSVFileSink(true, previous.getSchema().getFieldNames()))
            .withNumShards(1)
            .to(url)
            .withPrefix(prefix)
            .withSuffix(".csv"));
  }

  private void buildJsonLoader() {
    url = StepFileConfigToUrl.gcsDestinationUrlWithoutPrefix(step.config);
    this.output
        .apply(AsJsons.of(this.errors))
        .apply(
            FileIO.<String>write()
                .via(new TextFileSink())
                .to(url)
                .withPrefix(prefix)
                .withSuffix(".json")
                .withNumShards(1));
  }
}
