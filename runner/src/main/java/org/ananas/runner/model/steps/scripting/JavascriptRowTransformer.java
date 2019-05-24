package org.ananas.runner.model.steps.scripting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.FlattenMode;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.script.ScriptException;
import org.ananas.runner.model.schema.JsonAutodetect;
import org.ananas.runner.model.schema.SchemaBasedRowConverter;
import org.ananas.runner.model.steps.commons.AbstractStepRunner;
import org.ananas.runner.model.steps.commons.StepRunner;
import org.ananas.runner.model.steps.commons.StepType;
import org.ananas.runner.model.steps.commons.json.AsJsons;
import org.ananas.runner.model.steps.commons.json.JsonStringBasedFlattenerReader;
import org.apache.beam.sdk.schemas.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavascriptRowTransformer extends AbstractStepRunner implements Serializable {
  private static final Logger LOG = LoggerFactory.getLogger(JavascriptRowTransformer.class);
  private static final long serialVersionUID = -7010768938364851288L;

  public JavascriptRowTransformer(
      String stepId,
      StepRunner previous,
      String functionName,
      String sample,
      List<String> scripts) {
    super(StepType.Transformer);
    this.stepId = stepId;
    Schema outputSchema = validate(previous.getSchema(), functionName, sample, scripts);

    this.output =
        previous
            .getOutput()
            .apply("row To String", AsJsons.of(this.errors))
            .apply(
                "javascript transform",
                new JavascriptTextTransform(functionName, scripts, this.errors))
            .apply(
                "string to Row",
                new JsonStringBasedFlattenerReader(
                    SchemaBasedRowConverter.of(outputSchema), this.errors));
    this.output.setRowSchema(outputSchema);
  }

  /**
   * Validate JS functions and returns output schemas
   *
   * @param inputSchema
   * @param functionName
   * @param scripts
   * @return output Schema
   */
  private Schema validate(
      Schema inputSchema, String functionName, String sample, List<String> scripts) {
    JavascriptRuntime javascriptRuntime = null;
    ObjectMapper customMapper = new ObjectMapper();
    try {
      javascriptRuntime = new JavascriptRuntime(functionName, scripts);
    } catch (ScriptException e) {
      throw new RuntimeException(e);
    }

    Map<String, Schema.FieldType> types = new HashMap<>();

    try {
      List<String> jsons = javascriptRuntime.invoke(sample);
      for (int j = 0; j < jsons.size(); j++) {
        // infer types here
        JsonAutodetect.inferTypes(types, jsons.get(j), FlattenMode.KEEP_ARRAYS, false);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return JsonAutodetect.buildSchema(types);
  }
}
