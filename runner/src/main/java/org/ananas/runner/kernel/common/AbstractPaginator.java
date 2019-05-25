package org.ananas.runner.kernel.common;

import org.ananas.runner.kernel.model.Dataframe;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import org.apache.commons.lang3.tuple.MutablePair;

public abstract class AbstractPaginator implements Paginator {
  protected static int DEFAULT_LIMIT = new Integer(100);
  protected String id;
  protected Schema schema;

  public AbstractPaginator(String id, Schema schema) {
    this.id = id;
    this.schema = schema;
  }

  public Schema getSchema() {
    return this.schema;
  }

  public abstract Iterable<Row> iterateRows(Integer page, Integer pageSize);

  public MutablePair<Schema, Iterable<Row>> paginateRows(Integer page, Integer pageSize) {
    Iterable<Row> rows = iterateRows(page, pageSize);
    return MutablePair.of(this.schema, rows);
  }

  public Dataframe paginate(Integer page, Integer pageSize) {
    return Dataframe.Of(this.id, paginateRows(page, pageSize));
  }
}
