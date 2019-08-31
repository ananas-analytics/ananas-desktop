package org.ananas.runner.steprunner.files.json;

import org.ananas.runner.core.ConnectorStepRunner;
import org.ananas.runner.core.common.JsonStringBasedFlattenerReader;
import org.ananas.runner.core.common.Sampler;
import org.ananas.runner.core.model.Step;
import org.ananas.runner.core.paginate.AutoDetectedSchemaPaginator;
import org.ananas.runner.core.paginate.PaginatorFactory;
import org.ananas.runner.core.schema.SchemaBasedRowConverter;
import org.ananas.runner.steprunner.files.txt.TruncatedTextIO;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.PCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonConnector extends ConnectorStepRunner {

  private static final Logger LOG = LoggerFactory.getLogger(JsonConnector.class);

  public static final String CONFIG_PATH = "path";
  private static final long serialVersionUID = -8661090069401345444L;

  public JsonConnector(Pipeline pipeline, Step step, boolean doSampling, boolean isTest) {
    super(pipeline, step, doSampling, isTest);
  }

  @Override
  public void build() {
    String url = (String) this.step.config.get(JsonConnector.CONFIG_PATH);

    Schema schema = step.getBeamSchema();
    if (schema == null || step.forceAutoDetectSchema()) {
      // find the paginator bind to it
      AutoDetectedSchemaPaginator paginator =
          PaginatorFactory.of(stepId, step.metadataId, step.type, step.config, schema)
              .buildPaginator();
      schema = paginator.getSchema();
    }
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

  /*
  public static Schema autodetect(String uri) {

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
   */
}
