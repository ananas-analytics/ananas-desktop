package org.ananas.runner.paginator.files;

import java.util.List;
import java.util.Map;
import org.ananas.runner.kernel.common.JsonStringBasedFlattenerReader;
import org.ananas.runner.kernel.errors.ErrorHandler;
import org.ananas.runner.kernel.paginate.AbstractPaginator;
import org.ananas.runner.kernel.paginate.AutoDetectedSchemaPaginator;
import org.ananas.runner.kernel.schema.JsonAutodetect;
import org.ananas.runner.kernel.schema.SchemaBasedRowConverter;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;

public class JsonPaginator extends AutoDetectedSchemaPaginator {

  private String url;
  JsonStringBasedFlattenerReader reader;
  protected ErrorHandler errors;

  public JsonPaginator(String id, String type, Map<String, Object> config, Schema schema) {
    super(id, type, config, schema);
    this.errors = new ErrorHandler();
    this.reader =
        new JsonStringBasedFlattenerReader(SchemaBasedRowConverter.of(this.schema), this.errors);
  }

  @Override
  public Schema autodetect() {
    List<String> lines =
        PageProcessor.readFile(this.url, 0, AbstractFilePaginator.DEFAULT_LIMIT, (e, i) -> e);
    return JsonAutodetect.autodetectJson(lines.iterator(), false, AbstractPaginator.DEFAULT_LIMIT);
  }

  @Override
  public void parseConfig(Map<String, Object> config) {
    this.url = (String) config.getOrDefault("path", "");
  }

  @Override
  public Iterable<Row> iterateRows(Integer page, Integer pageSize) {
    return PageProcessor.readFile(
        this.url, page, pageSize, (e, i) -> this.reader.document2BeamRow(e));
  }
}
