package org.ananas.runner.legacy.steps.scripting;

import com.google.common.base.Strings;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import javax.script.ScriptException;
import org.ananas.runner.kernel.errors.ErrorHandler;
import org.ananas.runner.kernel.errors.ExceptionHandler;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;

/** Loads code into the Nashorn Javascript Engine, and executes Javascript Functions. */
public class JavascriptTextTransform extends PTransform<PCollection<String>, PCollection<String>>
    implements Serializable {

  private static final long serialVersionUID = -3356423937373936449L;
  private final ErrorHandler errors;
  private String functionName;
  private Collection<String> scripts;

  JavascriptTextTransform(String functionName, Collection<String> scripts, ErrorHandler errors) {
    this.errors = errors;
    this.functionName = functionName;
    this.scripts = scripts;
  }

  private ErrorHandler getErrors() {
    return this.errors;
  }

  @Override
  public PCollection<String> expand(PCollection<String> input) {
    return input.apply(
        ParDo.of(
            new DoFn<String, String>() {
              private static final long serialVersionUID = -2149374618046492441L;
              private ErrorHandler errors;
              private JavascriptRuntime javascriptRuntime;

              @Setup
              public void setup() {
                this.errors = getErrors();
                try {
                  this.javascriptRuntime =
                      new JavascriptRuntime(
                          JavascriptTextTransform.this.functionName,
                          JavascriptTextTransform.this.scripts);
                } catch (ScriptException e) {
                  throw new RuntimeException(e);
                }
              }

              @ProcessElement
              public void processElement(ProcessContext c)
                  throws IOException, NoSuchMethodException, ScriptException {
                String element = c.element();

                try {
                  for (String json : this.javascriptRuntime.invoke(element)) {
                    if (!Strings.isNullOrEmpty(json)) {
                      c.output(json);
                    }
                  }
                } catch (ScriptException e) {
                  this.errors.addError(ExceptionHandler.ErrorCode.JAVASCRIPT, e.getMessage());
                }
              }
            }));
  }
}
