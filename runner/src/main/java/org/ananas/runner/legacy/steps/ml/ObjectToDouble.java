package org.ananas.runner.legacy.steps.ml;

import java.math.BigDecimal;
import org.apache.beam.sdk.schemas.Schema;

/** Double convertor */
// TODO fix this conversion
public class ObjectToDouble {

  public static double toDouble(Schema.Field f, Object o) {
    if (!f.getType().getTypeName().isNumericType()) {
      return Double.NaN;
    } else {
      if (f.getType().getTypeName().equals(Schema.TypeName.DOUBLE)) {
        return (Double) o;
      }
      if (f.getType().getTypeName().equals(Schema.TypeName.FLOAT)) {
        return ((Float) o).doubleValue();
      }
      if (f.getType().getTypeName().equals(Schema.TypeName.DECIMAL)) {
        return ((BigDecimal) o).doubleValue();
      }
      if (f.getType().getTypeName().equals(Schema.TypeName.INT16)) {
        return ((Integer) o).doubleValue();
      }
      if (f.getType().getTypeName().equals(Schema.TypeName.INT32)) {
        return ((Integer) o).doubleValue();
      }
      if (f.getType().getTypeName().equals(Schema.TypeName.INT64)) {
        return ((Long) o).doubleValue();
      }
    }
    return Double.NaN;
  }
}
