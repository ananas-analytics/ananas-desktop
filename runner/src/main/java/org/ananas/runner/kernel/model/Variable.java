package org.ananas.runner.kernel.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Variable {

  public String name;
  public String type;
  public String description;
  public String scope;
  public String value;

  public Variable() {}

  public Variable(String name, String type, String description, String scope, String value) {
    this.name = name;
    this.type = type;
    this.description = description;
    this.scope = scope;
    this.value = value;
  }

  public Date convertToDate() {
    if (this.value == null) {
      return new Date();
    }
    return new Date(Long.parseLong(this.value));
    // ISO8601
    /*
    DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
    OffsetDateTime offsetDateTime = OffsetDateTime.parse(this.value, timeFormatter);
    return Date.from(Instant.from(offsetDateTime));
     */
  }

  public Double convertToNumber() {
    if (this.value == null) {
      return 0.0;
    }
    return Double.parseDouble(this.value);
  }

  public String convertToString() {
    if (this.value == null) {
      return "null";
    }
    return this.value.toString();
  }

  public Boolean convertToBoolean() {
    if (this.value == null) {
      return false;
    }
    return Boolean.valueOf(this.value);
  }

  @Override
  public String toString() {
    return "Variable{"
        + "name='"
        + name
        + '\''
        + ", type='"
        + type
        + '\''
        + ", description='"
        + description
        + '\''
        + ", scope='"
        + scope
        + '\''
        + ", value='"
        + value
        + '\''
        + '}';
  }
}
