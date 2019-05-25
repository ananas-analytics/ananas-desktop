package org.ananas.runner.model.steps.files;

import java.io.File;
import java.io.Serializable;
import org.ananas.runner.kernel.AbstractStepRunner;
import org.ananas.runner.kernel.common.JsonStringBasedFlattenerReader;
import org.ananas.runner.kernel.common.Sampler;
import org.ananas.runner.kernel.model.StepType;
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

public class JsonConnector extends AbstractStepRunner implements Serializable {

  private static final Logger LOG = LoggerFactory.getLogger(JsonConnector.class);
  private static final long serialVersionUID = 6862857326752899622L;

  public JsonConnector(
      Pipeline pipeline, String stepId, String url, boolean doSampling, boolean isTest) {
    super(StepType.Connector);
    Schema schema = autodetect(url);
    this.stepId = stepId;
    PCollection<String> p =
        pipeline.apply(isTest ? TruncatedTextIO.read().from(url) : TextIO.read().from(url));
    this.output =
        Sampler.sample(p, DEFAULT_LIMIT, (doSampling || isTest))
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
