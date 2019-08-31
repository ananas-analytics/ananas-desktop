package org.ananas.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse<T> {
  public int code;
  public T data;
  public String message;
}
