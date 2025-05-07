package com.nailic.JwtAuth.DTOs;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
  private String token;
  private String refreshToken;
  private long userId;
  private Set<String> roles;

}
