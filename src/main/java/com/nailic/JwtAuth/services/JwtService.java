package com.nailic.JwtAuth.services;

import com.nailic.JwtAuth.entities.CurrentUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Logger;
import javax.crypto.KeyGenerator;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class JwtService {
  private static final Logger logger = Logger.getLogger(JwtService.class.getName());
  private static String secretKey = "";

  public JwtService() {
    try {
      KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
      secretKey = Base64.getEncoder().encodeToString(keyGenerator.generateKey().getEncoded());
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  public static Boolean validateToken(String token, UserDetails userDetails) {
    try {
      Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
      return getUserNameFromToken(token).equals(userDetails.getUsername());
    } catch (MalformedJwtException | IllegalArgumentException | UnsupportedJwtException e) {
      logger.info(e.getMessage());
    }
    return false;
  }

  public static String getUserNameFromToken(String token) {
    return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
  }

  private Key getSecretKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String generateToken(CurrentUser currentUser) {
    return Jwts.builder()
        .signWith(getSecretKey())
        .setSubject(currentUser.getUsername())
        .setIssuedAt(new Date())
        .setExpiration(
            new Date(
                System.currentTimeMillis()
                    + 15 * 60 * 1000)) // 15 minutes = 15 * 60 * 1000 milliseconds
        .compact();
  }
}
