package com.nailic.JwtAuth.Controller;

import com.nailic.JwtAuth.DTOs.AuthResponse;
import com.nailic.JwtAuth.DTOs.CurrentUserDto;
import com.nailic.JwtAuth.entities.CurrentUser;
import com.nailic.JwtAuth.entities.Role;
import com.nailic.JwtAuth.exceptions.NotFoundException;
import com.nailic.JwtAuth.services.CurrentUserService;
import com.nailic.JwtAuth.services.RefreshTokenService;
import java.util.Set;
import java.util.stream.Collectors;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

  @Autowired
  private CurrentUserService currentUserService;
  @Autowired
  private RefreshTokenService refreshTokenService;
  @Autowired
  private AuthenticationManager authenticationManager;

  @PostMapping("/signup")
  public CurrentUserDto signup(@RequestBody CurrentUserDto userDto) {
    ModelMapper userMapper = new ModelMapper();
        CurrentUser currentUser = currentUserService.registerUser(userDto);
    if (currentUser == null) {
      return null;
    }

    return userMapper.map(currentUser, CurrentUserDto.class);
  }

  @PostMapping("/login")
  public AuthResponse login(@RequestBody CurrentUserDto userDto) throws NotFoundException {
    ModelMapper userMapper = new ModelMapper();
    CurrentUser user = userMapper.map(userDto, CurrentUser.class);
    AuthResponse jwt = currentUserService.login(user, authenticationManager);
    if (jwt == null) {
      throw new NotFoundException("User not found");
    }

    return jwt;
  }

  @PostMapping("/refreshToken")
  public AuthResponse refreshToken(@RequestBody String refreshToken) throws NotFoundException {
    return refreshTokenService.refreshToken(refreshToken);
  }
}
