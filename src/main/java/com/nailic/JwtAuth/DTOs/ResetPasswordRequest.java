package com.nailic.JwtAuth.DTOs;

import lombok.Data;

@Data
public class ResetPasswordRequest {
  private String email;
  private String newPassword;
}
