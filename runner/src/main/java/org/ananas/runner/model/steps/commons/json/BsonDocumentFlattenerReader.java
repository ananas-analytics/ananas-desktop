package org.ananas.runner.model.steps.commons.json;

import com.github.wnameless.json.flattener.FlattenMode;
import com.github.wnameless.json.flattener.JsonFlattener;
import java.io.Serializable;
import org.ananas.runner.model.schema.SchemaBasedRowConverter;
import org.ananas.runner.model.steps.commons.ErrorHandler;
import org.apache.beam.sdk.values.Row;
import org.bson.Document;

public class BsonDocumentFlattenerReader extends AbstractJsonFlattenerReader<Document>
    implements Serializable {

  private static final long serialVersionUID = -6665406554798597832L;

  public BsonDocumentFlattenerReader(SchemaBasedRowConverter converter, ErrorHandler errorHandler) {
    super(converter, errorHandler);
  }

  @Override
  public Row document2BeamRow(Document doc) {
    try {
      return this.converter.convertMap(
          new JsonFlattener(doc.toJson()).withFlattenMode(FlattenMode.KEEP_ARRAYS).flattenAsMap());
    } catch (Exception e) {
      this.errors.addError(e);
    }
    return null;
  }
}
