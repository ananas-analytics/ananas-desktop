package org.ananas.runner.core.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;

public class StepTest {

  @Test
  public void deepEquals() {
    String id = UUID.randomUUID().toString();

    Step one = getStep(id, "B");

    Step two = getStep(id, "C");

    Assert.assertEquals("same values same step", false, one.deepEquals(two));
    Assert.assertEquals("same id <=> equals step", true, one.equals(two));
  }

  @Test
  public void deepCollectionEquals() {
    String id = UUID.randomUUID().toString();

    Step one = getStep(id, "B");

    Step two = getStep(id, "C");

    Assert.assertEquals(
        "same values same step",
        false,
        Step.deepEquals(Arrays.asList(two, one), Arrays.asList(one)));
    Assert.assertEquals(
        "same values same step",
        true,
        Step.deepEquals(Arrays.asList(two, one), Arrays.asList(two, one)));
    Assert.assertEquals(
        "same values same step",
        false,
        Step.deepEquals(Arrays.asList(two, one), Arrays.asList(two)));
    Assert.assertEquals(
        "same values same step",
        false,
        Step.deepEquals(Arrays.asList(two), Arrays.asList(two, one)));
  }

  private Step getStep(String id, String b) {
    Step one = new Step();
    one.id = id;
    one.config = new HashMap<>();
    one.config.put("A", b); // DIFF
    one.type = "connector";
    one.name = "desc";
    one.description = "desc";
    return one;
  }
}
