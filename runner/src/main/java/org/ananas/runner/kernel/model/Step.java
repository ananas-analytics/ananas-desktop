package org.ananas.runner.kernel.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import lombok.Data;
import org.apache.beam.sdk.schemas.Schema;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Step implements DeepComparable, Serializable {

  private static final long serialVersionUID = -3839588188691082907L;

  // Reserved configuration name
  public static final String FORCE_AUTODETECT_SCHEMA = "forceAutoDetectSchema";

  public String id;
  public String metadataId;
  public String name;
  public String type;
  public String description;
  public Dataframe dataframe;
  public Map<String, Object> config;

  public Step() {}

  public static Step of(String id, String metadataId) {
    Step step = new Step();
    step.id = id;
    step.metadataId = metadataId;
    step.config = new HashMap<>();
    return step;
  }

  public Object getConfigParam(String o) {
    return this.config.get(o);
  }

  public boolean forceAutoDetectSchema() {
    // TODO: turn default to false, after providing the possibility to use existing schema
    //  in paginator
    return (Boolean) config.getOrDefault(FORCE_AUTODETECT_SCHEMA, false);
  }

  /**
   * Get the output schema, or null if not defined
   *
   * @return
   */
  public Schema getBeamSchema() {
    if (dataframe == null) {
      return null;
    }
    if (dataframe.schema == null) {
      return null;
    }
    return dataframe.schema.toBeamSchema();
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
    if (this.metadataId != null ? !this.metadataId.equals(step.metadataId) : step.id != null) {
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

  @Override
  public String toString() {
    return "Step{"
        + "id='"
        + id
        + '\''
        + ", metadataId='"
        + metadataId
        + '\''
        + ", name='"
        + name
        + '\''
        + '}';
  }
}
