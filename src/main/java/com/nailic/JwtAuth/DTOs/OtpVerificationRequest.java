package com.nailic.JwtAuth.DTOs;

import lombok.Data;

@Data
public class OtpVerificationRequest {
  private String email;
  private String otp;

}
