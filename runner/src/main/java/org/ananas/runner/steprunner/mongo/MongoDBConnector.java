package org.ananas.runner.steprunner.mongo;

import com.google.common.base.Preconditions;
import org.ananas.runner.kernel.ConnectorStepRunner;
import org.ananas.runner.kernel.common.BsonDocumentFlattenerReader;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.paginate.AutoDetectedSchemaPaginator;
import org.ananas.runner.kernel.paginate.PaginatorFactory;
import org.ananas.runner.kernel.schema.SchemaBasedRowConverter;
import org.ananas.runner.legacy.steps.commons.json.BsonDocumentAsTextReader;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.RowCoder;
import org.apache.beam.sdk.schemas.Schema;

public class MongoDBConnector extends ConnectorStepRunner {

  private static final long serialVersionUID = -288222796483489197L;

  private MongoStepConfig config;

  public MongoDBConnector(Pipeline pipeline, Step step, boolean doSampling, boolean isTest) {
    super(pipeline, step, doSampling, isTest);
  }

  public void build() {
    config = new MongoStepConfig(this.step.config);

    Schema schema = step.getBeamSchema();
    if (schema == null || step.forceAutoDetectSchema()) {
      AutoDetectedSchemaPaginator paginator =
          PaginatorFactory.of(stepId, step.metadataId, step.type, step.config, schema)
              .buildPaginator();
      // find the paginator bind to it
      schema = paginator.getSchema();
    }
    Preconditions.checkNotNull(schema);
    this.output =
        pipeline
            .apply(
                MongoReadLimited(
                    config.getUrl(),
                    config.database,
                    config.collection,
                    config.filters,
                    doSampling || isTest))
            .apply(
                config.isText
                    ? new BsonDocumentAsTextReader(schema)
                    : new BsonDocumentFlattenerReader(
                        SchemaBasedRowConverter.of(schema), this.errors));
    this.output.setCoder(RowCoder.of(schema));
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
