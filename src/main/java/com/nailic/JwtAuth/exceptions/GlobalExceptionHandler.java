package com.nailic.JwtAuth.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<String> handleDataIntegrityViolationException(
      DataIntegrityViolationException ex) {
    String message = "An error has occured";
    Throwable rootCause = ex.getRootCause();
    if (rootCause instanceof DataIntegrityViolationException) {
      String rootMessage = rootCause.getMessage();
      if (rootMessage != null && rootMessage.contains("username")) {
        message = "Username already exists";
      } else if (rootMessage != null && rootMessage.contains("email")) {
        message = "Email already exists";
      }
    }
    return new ResponseEntity<>(message, HttpStatus.CONFLICT);
  }
  @ExceptionHandler(OTPExpiredException.class)
  public ResponseEntity<String> handleOTPExpiredException(
      DataIntegrityViolationException ex) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OTP expired");
  }
}
