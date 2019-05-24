package org.ananas.runner.model.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Step implements DeepComparable {
  public String id;
  public String name;
  public String type;
  public String description;
  public Map<String, Object> config;

  public Step() {}

  public static Step of(String id) {
    Step step = new Step();
    step.id = id;
    step.config = new HashMap<>();
    return step;
  }

  public Object getConfigParam(String o) {
    return this.config.get(o);
  }

  @Override
  public boolean deepEquals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Step)) {
      return false;
    }

    Step step = (Step) o;

    if (this.id != null ? !this.id.equals(step.id) : step.id != null) {
      return false;
    }
    if (this.name != null ? !this.name.equals(step.name) : step.name != null) {
      return false;
    }
    if (this.type != null ? !this.type.equals(step.type) : step.type != null) {
      return false;
    }
    if (this.description != null
        ? !this.description.equals(step.description)
        : step.description != null) {
      return false;
    }
    return this.config != null ? this.config.equals(step.config) : step.config == null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Step)) {
      return false;
    }

    Step step = (Step) o;

    return this.id != null ? this.id.equals(step.id) : step.id == null;
  }

  @Override
  public int hashCode() {
    return this.id != null ? this.id.hashCode() : 0;
  }

  /**
   * Compare two collections of steps
   *
   * @param steps
   * @param otherSteps
   * @return true if both collections are deeply equals (using deepEquals)
   */
  public static boolean deepEquals(Iterable<Step> steps, Iterable<Step> otherSteps) {
    Iterator<Step> stepsIterator = steps.iterator();
    Iterator<Step> other = otherSteps.iterator();
    while (other.hasNext()) {
      if (!stepsIterator.hasNext()) {
        return false;
      }
      Step next = stepsIterator.next();
      Step next1 = other.next();
      if (!next.deepEquals(next1)) {
        return false;
      }
    }
    if (stepsIterator.hasNext()) {
      return false;
    }
    return true;
  }
}
