package org.ananas.runner.core.common;

import org.apache.beam.sdk.transforms.Sample;
import org.apache.beam.sdk.values.PCollection;

public class Sampler {

  public static <T> PCollection<T> sample(PCollection<T> rows, int limit, boolean sample) {
    if (sample) {
      return rows.apply(Sample.any(limit));
    }
    return rows;
  }
}
