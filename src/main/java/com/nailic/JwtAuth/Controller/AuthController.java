package com.nailic.JwtAuth.Controller;

import com.nailic.JwtAuth.DTOs.AuthResponse;
import com.nailic.JwtAuth.DTOs.CurrentUserDto;
import com.nailic.JwtAuth.DTOs.ForgotPasswordRequest;
import com.nailic.JwtAuth.DTOs.ResetPasswordRequest;
import com.nailic.JwtAuth.entities.CurrentUser;
import com.nailic.JwtAuth.exceptions.NotFoundException;
import com.nailic.JwtAuth.exceptions.OTPExpiredException;
import com.nailic.JwtAuth.services.CurrentUserService;
import com.nailic.JwtAuth.services.OtpService;
import com.nailic.JwtAuth.services.RefreshTokenService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  @Autowired
  private OtpService otpService;

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
  public AuthResponse refreshToken(@RequestBody String refreshToken) {
    return refreshTokenService.refreshToken(refreshToken);
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {

    try {

      otpService.sendForgotPasswordEmail(request.getEmail());
      return ResponseEntity.ok("password email sent");
    } catch (OTPExpiredException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("OTP expired");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  @PostMapping("/verify-otp")
  public ResponseEntity<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {

    try {

      otpService.verifyOtp(email, otp);
      return ResponseEntity.ok("otp verified");
    } catch (OTPExpiredException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("OTP verification failed");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  @PostMapping("/reset-pw")
  public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {

    try {

      otpService.resetPassword(request);
      return ResponseEntity.ok("password reset successful");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }
}
