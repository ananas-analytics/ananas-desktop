package org.ananas.runner.model.steps.ml;

import java.io.Serializable;
import java.text.ParseException;
import java.util.*;
import org.ananas.runner.kernel.AbstractStepRunner;
import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.model.StepType;
import org.ananas.runner.misc.SerializationUtils;
import org.ananas.runner.model.api.model.StepConfig;
import org.apache.beam.sdk.coders.CannotProvideCoderException;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.beam.sdk.values.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smile.data.Attribute;

public class MLModelPredictor extends AbstractStepRunner implements StepRunner, Serializable {
  private static final Logger LOG = LoggerFactory.getLogger(MLModelPredictor.class);
  private static final long serialVersionUID = -2846511153859594113L;

  private int yColIndex;
  private boolean isYInput; // YColumn alreay input in previous schema

  public MLModelPredictor(Step MLStep, StepRunner previous) {
    super(StepType.Transformer);
    this.stepId = MLStep.id;

    final String yColumnName = (String) MLStep.config.get(StepConfig.ML_Y_COLUMN_NAME);

    final Predictor model =
        (Predictor)
            SerializationUtils.deserialize(
                MLHookTemplate.getSerializedModelPath(this.stepId), true);

    Schema.Builder schemaBuilder = Schema.builder();
    final List<Schema.Field> previousSchemaFields = previous.getSchema().getFields();
    for (Schema.Field f : previousSchemaFields) {
      if (f.getName().equalsIgnoreCase(yColumnName)) {
        schemaBuilder.addNullableField(f.getName(), model.getPredictedType());
      } else {
        schemaBuilder.addNullableField(f.getName(), Schema.FieldType.DOUBLE);
      }
    }
    final Schema schema = schemaBuilder.build();

    // resolves yColIndex Y response variable index in input file if it exists otherwise we add it
    // at last Index
    if (schema.getFieldNames().contains(yColumnName)) {
      for (int i = 0; i < schema.getFieldCount(); i++) {
        if (schema.getField(i).getName().equalsIgnoreCase(yColumnName)) {
          this.yColIndex = i;
          this.isYInput = true;
          break;
        }
      }
    } else {
      this.isYInput = false;
      this.yColIndex = schema.getFieldCount();
    }

    Schema outputSchema =
        this.isYInput
            ? schema
            : MLHookTemplate.getSchemaBuilder(schema)
                .addNullableField(yColumnName, model.getPredictedType())
                .build();

    if (model == null) {
      throw new RuntimeException(
          "Oops. We can't find any trained model. Please train your model and get back to this step. ");
    }

    Combine.Globally<Row, List<Attribute>> globalAttributes =
        Combine.globally(new CombineAttributeFn());

    Combine.GloballyAsSingletonView<Row, List<Attribute>> t = globalAttributes.asSingletonView();

    final PCollectionView<List<Attribute>> attView = previous.getOutput().apply(t);

    this.output =
        previous
            .getOutput()
            .apply(
                "predict",
                ParDo.of(
                        (new DoFn<Row, Row>() {
                          private static final long serialVersionUID = 7683479078488097137L;

                          @ProcessElement
                          public void processElement(
                              @Element Row input, OutputReceiver<Row> out, ProcessContext c) {
                            List<Attribute> atts = c.sideInput(attView);
                            double[] x = rowToVector(previousSchemaFields, input, atts);
                            out.output(predictRow(outputSchema, model, x));
                          }
                        }))
                    .withSideInputs(attView));

    this.output.setRowSchema(outputSchema);
  }

  // an accumulator to first get all values and then merge them before doing the prediction
  public static class CombineAttributeFn
      extends Combine.CombineFn<Row, CombineAttributeFn.Accum, List<Attribute>> {
    private static final long serialVersionUID = -9150309514299724290L;

    public static class Accum implements Serializable {
      private static final long serialVersionUID = -364323630388446691L;
      List<Set<String>> values = new ArrayList<>();
      List<Attribute> atts = new ArrayList<>();
    }

    @Override
    public Coder<CombineAttributeFn.Accum> getAccumulatorCoder(
        CoderRegistry registry, Coder<Row> inputCoder) throws CannotProvideCoderException {
      return SerializableCoder.of(Accum.class);
    }

    @Override
    public Accum createAccumulator() {
      return new Accum();
    }

    @Override
    public Accum addInput(Accum accum, Row row) {
      if (accum.values.isEmpty()) {
        for (Schema.Field f : row.getSchema().getFields()) {
          accum.values.add(new HashSet<>());
          accum.atts.add(MLHookTemplate.attribute(f, false));
        }
      }
      for (int i = 0; i < row.getFieldCount(); i++) {
        if (row.getSchema().getField(i).getType().getTypeName().isStringType()) {
          accum.values.get(i).add(row.getString(i));
        } else {
          accum.values.get(i).add(row.getValue(i).toString());
        }
      }
      return accum;
    }

    @Override
    public Accum mergeAccumulators(Iterable<Accum> accums) {
      Accum merged = null;

      for (Accum acc : accums) {
        if (merged == null && acc != null) {
          merged = acc;
        } else {
          for (int i = 0; i < acc.values.size(); i++) {
            merged.values.get(i).addAll(acc.values.get(i));
          }
        }
      }
      return merged;
    }

    @Override
    public List<Attribute> extractOutput(Accum accum) {
      for (int i = 0; i < accum.values.size(); i++) {
        Iterator<String> it = accum.values.get(i).iterator();
        while (it.hasNext()) {
          try {
            accum.atts.get(i).valueOf(it.next());
          } catch (ParseException e) {
            LOG.warn("row value cannot be casted to string");
          }
        }
      }
      return accum.atts;
    }
  }

  /**
   * @param schema input file schema
   * @param row
   * @param attributeList
   * @return
   */
  protected double[] rowToVector(
      List<Schema.Field> schema, Row row, List<Attribute> attributeList) {
    double[] doubles = new double[this.isYInput ? row.getFieldCount() - 1 : row.getFieldCount()];
    int j = 0;
    for (int i = 0; i < row.getFieldCount(); i++) {
      if (this.yColIndex == i) {
        continue;
      }
      j = i > this.yColIndex ? i - 1 : i;
      Schema.TypeName typeName = schema.get(i).getType().getTypeName();
      if (typeName.isStringType()) {
        try {
          doubles[j] = attributeList.get(i).valueOf(row.getString(i));
        } catch (ParseException e) {
          doubles[j] = 0;
        }
      } else if (typeName.isNumericType()) {
        doubles[j] =
            Double.valueOf(
                row.getValue(i).toString()); // TODO use more accurate efficient conversion
      }
    }
    return doubles;
  }

  protected Row predictRow(Schema schemaWithColName, Predictor model, double[] x) {
    Row.Builder builder = Row.withSchema(schemaWithColName);
    for (int i = 0; i < schemaWithColName.getFieldCount(); i++) {
      builder.addValue(i == this.yColIndex ? model.predict(x) : x[i > this.yColIndex ? i - 1 : i]);
    }
    return builder.build();
  }
}
