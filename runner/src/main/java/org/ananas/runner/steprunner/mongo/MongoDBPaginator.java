package org.ananas.runner.steprunner.mongo;

import com.github.wnameless.json.flattener.FlattenMode;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.Iterator;
import java.util.Map;
import org.ananas.runner.kernel.common.BsonDocumentFlattenerReader;
import org.ananas.runner.kernel.errors.ErrorHandler;
import org.ananas.runner.kernel.paginate.AutoDetectedSchemaPaginator;
import org.ananas.runner.kernel.schema.JsonAutodetect;
import org.ananas.runner.kernel.schema.SchemaBasedRowConverter;
import org.ananas.runner.legacy.steps.commons.json.BsonDocumentAsTextReader;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDBPaginator extends AutoDetectedSchemaPaginator {

  private static final Logger LOG = LoggerFactory.getLogger(MongoDBPaginator.class);

  MongoStepConfig config;

  public MongoDBPaginator(String id, String type, Map<String, Object> config, Schema schema) {
    super(id, type, config, schema);
  }

  @Override
  public void parseConfig(Map<String, Object> config) {
    this.config = new MongoStepConfig(config);
  }

  @Override
  public Iterable<Row> iterateRows(Integer page, Integer pageSize) {
    LOG.info("iterate rows page {} pagesize {}", page, pageSize);
    LOG.info("schema {}", this.schema);
    FindIterable<Document> it = find().skip(pageSize * page).limit(pageSize);
    if (this.config.isText) {
      BsonDocumentAsTextReader reader = new BsonDocumentAsTextReader(this.schema);
      return it.map(e -> reader.doc2Row(e));
    }
    BsonDocumentFlattenerReader reader =
        new BsonDocumentFlattenerReader(
            SchemaBasedRowConverter.of(this.schema), new ErrorHandler());
    return it.map(e -> reader.document2BeamRow(e));
  }

  @Override
  public Schema autodetect() {
    LOG.info("autodetect schema");
    if (config.isText) {
      return Schema.builder().addNullableField("text", Schema.FieldType.STRING).build();
    }
    FindIterable<Document> l = find();
    Iterator<Document> it = l.limit(DEFAULT_LIMIT).iterator();
    return JsonAutodetect.autodetectBson(it, FlattenMode.KEEP_ARRAYS, false, DEFAULT_LIMIT);
  }

  private FindIterable<Document> find() {
    MongoClient mongoClient = new MongoClient(new MongoClientURI(this.config.getUrl()));
    MongoDatabase db = mongoClient.getDatabase(this.config.database);
    MongoCollection collection = db.getCollection(this.config.collection);
    if (this.config.filters == null) {
      return collection.find();
    }
    Document bson = Document.parse(this.config.filters);
    return collection.find(bson);
  }
}
