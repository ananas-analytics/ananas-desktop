package org.ananas.runner.model.steps.db;

import java.io.Serializable;
import org.ananas.runner.kernel.AbstractStepRunner;
import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.kernel.common.BsonDocumentFlattenerReader;
import org.ananas.runner.kernel.model.StepType;
import org.ananas.runner.kernel.schema.SchemaBasedRowConverter;
import org.ananas.runner.model.steps.commons.json.BsonDocumentAsTextReader;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;

public class MongoDBConnector extends AbstractStepRunner implements StepRunner, Serializable {

  private static final long serialVersionUID = -288222796483489197L;

  /** constructor */
  public MongoDBConnector(
      Pipeline pipeline,
      String stepId,
      MongoStepConfig config,
      boolean doSampling,
      boolean isTest) {
    super(StepType.Connector);
    String uri = config.getUrl();
    MongoDBPaginator paginator = new MongoDBPaginator(stepId, config);
    Schema schema = paginator.autodetect();
    this.stepId = stepId;
    this.output =
        pipeline
            .apply(
                MongoReadLimited(
                    uri, config.database, config.collection, config.filters, doSampling || isTest))
            .apply(
                config.isText
                    ? new BsonDocumentAsTextReader(schema)
                    : new BsonDocumentFlattenerReader(
                        SchemaBasedRowConverter.of(schema), this.errors));
    this.output.setRowSchema(schema);
  }

  private MongoDbIO.Read MongoReadLimited(
      String uri, String database, String collection, String filters, boolean isLimit) {
    MongoDbIO.Read r =
        MongoDbIO.read()
            .withUri(uri)
            .withFilter(filters)
            .withDatabase(database)
            .withCollection(collection);
    return !isLimit ? r : r.withLimit(DEFAULT_LIMIT);
  }
}
