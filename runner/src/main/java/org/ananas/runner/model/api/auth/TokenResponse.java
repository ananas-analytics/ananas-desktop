package org.ananas.runner.model.api.auth;

import lombok.Data;

@Data
public class TokenResponse {
  public String code;
  public Token data;
}
