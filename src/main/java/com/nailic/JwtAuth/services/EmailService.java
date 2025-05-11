package com.nailic.JwtAuth.services;

import com.nailic.JwtAuth.entities.CurrentUser;
import com.nailic.JwtAuth.exceptions.NotFoundException;
import com.nailic.JwtAuth.repos.CurrentUserRepo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class EmailService {

  @Autowired
  private JavaMailSender mailSender;

  @Autowired
  private CurrentUserRepo currentUserRepo;
  @Value("${app.verification-url}")
  private String verificationUrl;

  public void sendOtpEmail(String toEmail, String otp) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(toEmail);
    message.setSubject("OTP Code");
    message.setText("OTP Code" + otp);
    mailSender.send(message);
  }

  public void sendVerificationEmail(String email) throws NotFoundException {

    CurrentUser user = currentUserRepo.findByEmail(email);
    if(user == null ) {
      throw new RuntimeException("user Not found");
    }
    String token = generateVerficationToken();
    user.setVerificationToken(token);
    user.setVerificationTokenExpiry(LocalDateTime.now().plusDays(1));
    currentUserRepo.save(user);
    String verificationLink = verificationUrl + "?token=" + token;
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(user.getEmail());
      helper.setSubject("Account Verification");
      String emailContent = "<html><body>"
          + "<h2>Welcome to Our Service !< /h2>"
          + "<p>Please click the following link to verify your account: </p>"
          + "<a href=\"" + verificationLink + "\">Verify Account</a>"
          + "<p>Or copy this URL to your browser: <br>"
          + verificationLink + "</p>"
          + "</body></html>";
      helper.setText(emailContent, true);
      mailSender.send(message);

    } catch (MessagingException e) {
      throw new RuntimeException("failed to send email", e);
    }
  }


  private String generateVerficationToken() {
    return UUID.randomUUID().toString();
  }

  public boolean verifyUser(String token) {
    CurrentUser user = currentUserRepo.findByVerificationToken(token);
    if (user == null) {
      throw new RuntimeException("user not found");
    }
    if (user.getVerificationTokenExpiry() == null) {
      throw new RuntimeException("token is null");
    }
    if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
      throw new RuntimeException("verification email Token expired");
    }
    if (user.getIsVerified()) {
      throw new RuntimeException("account is already verified");
    }
    user.setIsVerified(true);
    user.setVerificationToken(null);
    user.setVerificationTokenExpiry(null);
    currentUserRepo.save(user);
    return true;
  }

  public void resendVerificationEmail(String email) throws NotFoundException {
    CurrentUser user = currentUserRepo.findByEmail(email);
    String verificationLink = verificationUrl + "?token=" + user.getVerificationToken();

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);

      helper.setTo(user.getEmail());
      helper.setSubject("Account Verification - New Link");

      String emailContent = "<html><body>"
          + "<h2>New Verification Link</h2>"
          + "<p>Here's your new verification link :< /p>"
          + "<a href=\"" + verificationLink + "\">Verify Account</a>"
          + "<p>Or copy this URL to your browser: <br>"
          + verificationLink + "</p>"
          + "<p>This link will expire in 24 hours. </p>"
          + "</body></html>";

      helper.setText(emailContent, true);
      mailSender.send(message);
    } catch (MessagingException e) {
      throw new RuntimeException("Failed to resend verification email", e);
    }

  }
}
