package org.ananas.runner.steprunner;

import java.io.File;
import org.ananas.runner.kernel.ConnectorStepRunner;
import org.ananas.runner.kernel.common.JsonStringBasedFlattenerReader;
import org.ananas.runner.kernel.common.Sampler;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.schema.JsonAutodetect;
import org.ananas.runner.kernel.schema.SchemaBasedRowConverter;
import org.ananas.runner.model.steps.files.txt.TruncatedTextIO;
import org.ananas.runner.model.steps.files.utils.FileIterator;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.PCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonConnector extends ConnectorStepRunner {

  private static final Logger LOG = LoggerFactory.getLogger(JsonConnector.class);

  public static final String CONFIG_PATH = "path";

  public JsonConnector(Pipeline pipeline, Step step, boolean doSampling, boolean isTest) {
    super(pipeline, step, doSampling, isTest);
  }

  public void build() {
    String url = (String) this.step.config.get(JsonConnector.CONFIG_PATH);

    Schema schema = autodetect(url);
    PCollection<String> p =
        this.pipeline.apply(
            this.isTest ? TruncatedTextIO.read().from(url) : TextIO.read().from(url));
    this.output =
        Sampler.sample(p, DEFAULT_LIMIT, (this.doSampling || this.isTest))
            .apply(
                new JsonStringBasedFlattenerReader(
                    SchemaBasedRowConverter.of(schema), this.errors));
    this.output.setRowSchema(schema);
  }

  private static Schema autodetect(String uri) {

    File f = new File(uri);
    if (!f.canRead()) {
      throw new RuntimeException("Can't read file " + uri);
    }

    long ts = System.currentTimeMillis();

    if (!f.exists()) {
      throw new RuntimeException(String.format("file %s does not exist", f.getPath()));
    }
    FileIterator it = new FileIterator(uri, f);

    try {
      return JsonAutodetect.autodetectJson(it.iterator(), false, DEFAULT_LIMIT);
    } finally {
      it.close();
      LOG.debug("autodetect : {} ms", System.currentTimeMillis() - ts);
    }
  }
}
