package org.ananas.runner.legacy.api.auth;

import lombok.Data;

@Data
public class User {
  public String email;
  public String password;

  public static User Of(String email, String password) {
    User u = new User();
    u.email = email;
    u.password = password;
    return u;
  }

  // '{"email": "test@email.com", "password": "456"}
}
