package com.nailic.JwtAuth.services;

import com.nailic.JwtAuth.DTOs.AuthResponse;
import com.nailic.JwtAuth.entities.CurrentUser;
import com.nailic.JwtAuth.entities.RefreshToken;
import com.nailic.JwtAuth.entities.TokenRefreshException;
import com.nailic.JwtAuth.repos.CurrentUserRepo;
import com.nailic.JwtAuth.repos.RefreshTokenRepo;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class RefreshTokenService {

  @Autowired
  private RefreshTokenRepo refreshTokenRepo;
  @Autowired
  private CurrentUserRepo currentUserRepo;
  @Autowired
  JwtService jwtService;


  public RefreshToken createRefreshToken(Long userId) {
    CurrentUser user = currentUserRepo.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    Optional<RefreshToken> existingRefreshToken = refreshTokenRepo.findByUser(user);

    RefreshToken refreshToken = existingRefreshToken.orElseGet(RefreshToken::new);
    refreshToken.setUser(user);
    refreshToken.setToken(UUID.randomUUID().toString());
    refreshToken.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));

    return refreshTokenRepo.save(refreshToken); // UPDATE if exists, INSERT if new
  }


  public boolean verifyExpiration(RefreshToken token) {
    if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
      refreshTokenRepo.delete(token);
      throw new TokenRefreshException(token.getToken(),
          "Refresh token was expired. Please make a new signin request");
    }

    return true;
  }

  public AuthResponse refreshToken(String refreshToken) {
    Optional<RefreshToken> newRefreshToken = refreshTokenRepo.findByToken(refreshToken);
    if(newRefreshToken.isPresent() && this.verifyExpiration(newRefreshToken.get())) {
      CurrentUser   user = newRefreshToken.get().getUser();
      String Token = jwtService.generateToken(user);
      return AuthResponse.builder()
          .refreshToken(newRefreshToken.get().getToken())
          .userId(user.getId())
          .roles(user.getRoles() != null ? user.getRoleNames() : null)
          .token(Token).build();

    }
    return null;

  }
}
