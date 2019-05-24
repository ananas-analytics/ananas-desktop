package org.ananas.runner.model.steps.files;

import java.util.List;
import org.ananas.runner.model.schema.JsonAutodetect;
import org.ananas.runner.model.schema.SchemaBasedRowConverter;
import org.ananas.runner.model.steps.commons.json.JsonStringBasedFlattenerReader;
import org.ananas.runner.model.steps.commons.paginate.AbstractPaginator;
import org.ananas.runner.model.steps.commons.paginate.PageProcessor;
import org.ananas.runner.model.steps.commons.paginate.Paginator;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;

public class JsonPaginator extends AbstractFilePaginator implements Paginator {

  JsonStringBasedFlattenerReader reader;

  public JsonPaginator(String id, String url) {
    super(id, url);
    this.schema = autodetect(AbstractPaginator.DEFAULT_LIMIT);
    this.reader =
        new JsonStringBasedFlattenerReader(SchemaBasedRowConverter.of(this.schema), this.errors);
  }

  @Override
  protected Schema autodetect(Integer pageSize) {
    List<String> lines = PageProcessor.readFile(this.url, 0, pageSize, (e, i) -> e);
    return JsonAutodetect.autodetectJson(lines.iterator(), false, AbstractPaginator.DEFAULT_LIMIT);
  }

  @Override
  public Iterable<Row> iterateRows(Integer page, Integer pageSize) {
    return PageProcessor.readFile(this.url, 0, pageSize, (e, i) -> this.reader.document2BeamRow(e));
  }
}
