package org.ananas.runner.kernel.common;

import java.util.Map;
import org.apache.beam.sdk.values.Row;

public interface RowConverter {

  Row convertMap(Map<String, Object> o);
}
