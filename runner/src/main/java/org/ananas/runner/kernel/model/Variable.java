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

  public Date convertToDate() {
    return new Date(Long.parseLong(this.value));
    // ISO8601
    /*
    DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
    OffsetDateTime offsetDateTime = OffsetDateTime.parse(this.value, timeFormatter);
    return Date.from(Instant.from(offsetDateTime));
     */
  }

  public Double convertToNumber() {
    return Double.parseDouble(this.value);
  }
}
