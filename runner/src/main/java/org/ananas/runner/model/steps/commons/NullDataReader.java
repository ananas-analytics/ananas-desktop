package org.ananas.runner.model.steps.commons;

import java.io.Serializable;
import java.util.List;
import org.ananas.runner.kernel.common.DataReader;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.Row;
import org.apache.commons.lang3.NotImplementedException;

public class NullDataReader implements Serializable, DataReader {

  private static final long serialVersionUID = 5543045542106682365L;

  private NullDataReader() {}

  public static DataReader of() {
    return new NullDataReader();
  }

  @Override
  public List<List<Object>> getData() {
    return null;
  }

  @Override
  public MapElements<Row, Void> mapElements() {
    throw new NotImplementedException("mapElements()");
  }
}
