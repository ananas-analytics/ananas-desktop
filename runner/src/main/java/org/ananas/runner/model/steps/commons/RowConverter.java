package org.ananas.runner.model.steps.commons;

import java.util.Map;
import org.apache.beam.sdk.values.Row;

public interface RowConverter {

  Row convertMap(Map<String, Object> o);
}
