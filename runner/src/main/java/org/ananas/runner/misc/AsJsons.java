package org.ananas.runner.misc;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.ananas.runner.core.common.Jsonifier;
import org.ananas.runner.core.errors.ErrorHandler;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.Row;

public class AsJsons extends PTransform<PCollection<Row>, PCollection<String>> {

  private static final long serialVersionUID = -8038472785957455609L;
  private Jsonifier jsonifier;
  private ObjectMapper customMapper;
  private ErrorHandler errors;

  public static AsJsons of(ErrorHandler errors) {
    AsJsons o = new AsJsons();
    o.customMapper = new ObjectMapper();
    o.jsonifier = Jsonifier.AsMap();
    o.errors = errors;
    return o;
  }

  @Override
  public PCollection<String> expand(PCollection<Row> input) {
    return (PCollection<String>)
        input.apply(
            MapElements.via(
                new SimpleFunction<Row, String>() {
                  private static final long serialVersionUID = -7405324806188252148L;

                  @Override
                  public String apply(Row i) {
                    try {
                      ObjectMapper mapper = AsJsons.this.customMapper;
                      return mapper.writeValueAsString(AsJsons.this.jsonifier.valueOfRow(i));
                    } catch (Exception e) {
                      AsJsons.this.errors.addError(e);
                      return null;
                    }
                  }
                }));
  }
}
