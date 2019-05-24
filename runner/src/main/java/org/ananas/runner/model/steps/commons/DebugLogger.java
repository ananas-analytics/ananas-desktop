package org.ananas.runner.model.steps.commons;

import javax.annotation.Nullable;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.Row;
import org.slf4j.LoggerFactory;

public class DebugLogger {

  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DebugLogger.class);

  public static MapElements<Row, Void> logRecords(String prefix) {
    return MapElements.<Row, Void>via(
        new SimpleFunction<Row, Void>() {
          private static final long serialVersionUID = 2375652577138428391L;

          @Override
          public @Nullable Void apply(Row input) {
            LOG.debug(prefix + ":" + input.getValues());
            return null;
          }
        });
  }
}
