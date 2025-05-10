package com.nailic.JwtAuth.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class NotFoundException extends Exception {
  public  NotFoundException(String message) {
    super(message);
  }

  @ResponseStatus(HttpStatus.FORBIDDEN)
  public static class TokenRefreshException extends RuntimeException {
      private static final long serialVersionUID = 1L;

      public TokenRefreshException(String token, String message) {
          super(String.format("Failed for [%s]: %s", token, message));
      }
  }
}
