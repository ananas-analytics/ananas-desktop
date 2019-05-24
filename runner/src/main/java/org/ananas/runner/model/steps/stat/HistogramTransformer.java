package org.ananas.runner.model.steps.stat;

import com.google.common.base.Preconditions;
import org.ananas.runner.model.core.Step;
import org.ananas.runner.model.steps.commons.AbstractStepRunner;
import org.ananas.runner.model.steps.commons.StepRunner;
import org.ananas.runner.model.steps.commons.StepType;
import org.ananas.runner.model.steps.ml.ObjectToDouble;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.Row;
import org.apache.beam.sdk.values.TypeDescriptors;

public class HistogramTransformer extends AbstractStepRunner implements StepRunner {

  private static final long serialVersionUID = -5626161538797830330L;

  public HistogramTransformer(Step step, StepRunner previous) {
    super(StepType.Transformer);
    this.stepId = step.id;
    String fieldname = (String) step.config.get("fieldname");
    Preconditions.checkNotNull(fieldname, "fieldname is expected. ");

    Integer binRange = (Integer) step.config.get("binrange");
    Preconditions.checkNotNull(binRange, "binrange required. ");

    final Schema schema = previous.getSchema();

    if (!schema.getFieldNames().contains(fieldname)) {
      throw new RuntimeException(String.format(" cannot find field %s.", fieldname));
    }

    if (!schema.getField(fieldname).getType().getTypeName().isNumericType()) {
      throw new RuntimeException(String.format(" %s field should be numeric.", fieldname));
    }

    final Schema outputSchema =
        Schema.builder()
            .addNullableField("value", Schema.FieldType.INT32)
            .addNullableField("frequency", Schema.FieldType.INT64)
            .build();

    PCollection<Integer> histogram =
        previous
            .getOutput()
            .apply(
                "binarize",
                MapElements.into(TypeDescriptors.integers())
                    .via(
                        (Row input) -> {
                          double d =
                              ObjectToDouble.toDouble(
                                  schema.getField(fieldname), input.getValue(fieldname));
                          return new Integer((((int) d) / binRange) * binRange);
                        }));

    this.output =
        histogram
            .apply("group value", Count.perElement())
            .apply(
                "convertToRow",
                MapElements.into(TypeDescriptors.rows())
                    .via(
                        kv ->
                            Row.withSchema(outputSchema)
                                .addValue(kv.getKey())
                                .addValue(kv.getValue())
                                .build()));

    this.output.setRowSchema(outputSchema);
  }
}
