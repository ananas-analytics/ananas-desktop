package org.ananas.runner.legacy.api.auth;

import lombok.Data;

@Data
public class TokenResponse {
  public String code;
  public Token data;
}
