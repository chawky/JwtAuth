package com.nailic.JwtAuth.exceptions;

public class OTPExpiredException extends RuntimeException {
  public OTPExpiredException(String message) {super(message);}

}
