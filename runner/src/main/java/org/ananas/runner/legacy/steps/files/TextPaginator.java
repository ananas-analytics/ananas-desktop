package org.ananas.runner.legacy.steps.files;

import org.ananas.runner.kernel.paginate.AbstractPaginator;
import org.ananas.runner.kernel.paginate.Paginator;
import org.ananas.runner.paginator.files.PageProcessor;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;

public class TextPaginator extends AbstractPaginator implements Paginator {
  String url;

  public TextPaginator(String id, String url) {
    super(id, Schema.builder().addField("text", Schema.FieldType.STRING).build());
    this.url = url;
  }

  @Override
  public Iterable<Row> iterateRows(Integer page, Integer pageSize) {
    return PageProcessor.readFile(
        this.url, page, pageSize, (e, i) -> Row.withSchema(this.schema).addValue(e).build());
  }

  /*	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
  	String path = "/home/grego/Documents/data/ratings.csv";
  	readFile(path, 150, 1000);
  }*/

}
