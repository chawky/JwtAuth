package com.nailic.JwtAuth.DTOs;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CurrentUserDto {
  private String username;
  private String email;
  private Set<String> roles;
  private String password;
}
