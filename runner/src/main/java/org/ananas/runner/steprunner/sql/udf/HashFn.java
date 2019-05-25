package org.ananas.runner.steprunner.sql.udf;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.nio.charset.Charset;
import org.apache.beam.sdk.transforms.SerializableFunction;

public class HashFn implements SerializableFunction<String, Integer> {
  private static final long serialVersionUID = -8064028600455474440L;

  HashFunction hash = Hashing.murmur3_32();

  @Override
  public Integer apply(String input) {
    if (input == null) {
      return 0;
    } else {
      return this.hash.hashString(input, Charset.forName("UTF-8")).asInt();
    }
  }
}
