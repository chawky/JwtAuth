package com.nailic.JwtAuth.services;

import com.nailic.JwtAuth.DTOs.ResetPasswordRequest;
import com.nailic.JwtAuth.entities.CurrentUser;
import com.nailic.JwtAuth.exceptions.NotFoundException;
import com.nailic.JwtAuth.repos.CurrentUserRepo;
import java.time.LocalDateTime;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class OtpService {
  @Autowired
  private CurrentUserRepo currentUserRepo;

  @Autowired
  private EmailService emailService;
  @Autowired
  private PasswordEncoder passwordEncoder;

  public void sendForgotPasswordEmail(String email) throws NotFoundException {
    CurrentUser user = currentUserRepo.findByEmail(email);
    String otp = generateOtp();
    user.setOtp(otp);
    user.setOtpExpiry(LocalDateTime.now().plusMinutes(1));
    currentUserRepo.save(user);
    emailService.sendOtpEmail(user.getEmail(), otp);

  }
  private String generateOtp() {
    Random rand = new Random();
    int otp = 100000 + rand.nextInt(900000);
    return String.valueOf(otp);
  }
  public void verifyOtp(String email ,String otp) throws NotFoundException {
    CurrentUser user = currentUserRepo.findByEmail(email);
    if(!user.getOtp().equals(otp)) {
      throw new NotFoundException("OTP verification failed : invalid otp");
    }
    if (user.getOtp() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
      throw new NotFoundException("OTP verification failed: otp expired");
    }
  }
  public void resetPassword(ResetPasswordRequest resetPasswordRequest) throws NotFoundException {
    CurrentUser user = currentUserRepo.findByEmail(resetPasswordRequest.getEmail());
    user.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
    currentUserRepo.save(user);
  }

}
