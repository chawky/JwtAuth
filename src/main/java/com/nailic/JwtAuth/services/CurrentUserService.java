package com.nailic.JwtAuth.services;

import com.nailic.JwtAuth.entities.CurrentUser;
import com.nailic.JwtAuth.exceptions.NotFoundException;
import com.nailic.JwtAuth.repos.CurrentUserRepo;
import jakarta.transaction.Transactional;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CurrentUserService implements UserDetailsService {
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  @Autowired private CurrentUserRepo currentUserRepo;
  @Autowired private JwtService jwtService;

  public CurrentUserService(BCryptPasswordEncoder bCryptPasswordEncoder) {
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  /**
   * @param email
   * @return CurrentUser
   */
  public CurrentUser findByEmail(String email) throws NotFoundException {
    CurrentUser user = currentUserRepo.findByEmail(email);
    if (Objects.nonNull(user)) {
      return user;
    } else {
      throw new NotFoundException("email not found ");
    }
  }

  /**
   * @param username
   * @param password
   * @return CurrentUser
   */
  public CurrentUser findByUsernameAndPassword(String username, String password)
      throws NotFoundException {
    CurrentUser user = currentUserRepo.findByUsernameAndPassword(username, password);
    if (Objects.nonNull(user)) {
      return user;
    } else {
      throw new NotFoundException(
          "no user withe this email " + username + " and password " + password + " are found ");
    }
  }

  /**
   * called by spring whenever you want to make a request it will use it to find the user
   *
   * @param username
   * @return CurrentUser
   * @throws UsernameNotFoundException
   */
  @Override
  public CurrentUser loadUserByUsername(String username) throws UsernameNotFoundException {
    CurrentUser user = currentUserRepo.findByUsername(username);
    if (Objects.isNull(user)) {
      throw new UsernameNotFoundException("User not found: " + username);
    }
    return user;
  }

  public CurrentUser registerUser(CurrentUser currentUser) {
    currentUser.setPassword(bCryptPasswordEncoder.encode(currentUser.getPassword()));
    return currentUserRepo.save(currentUser);
  }

  public String login(CurrentUser currentUser, AuthenticationManager authenticationManager)
      throws NotFoundException {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                currentUser.getUsername(), currentUser.getPassword()));
    if (authentication.isAuthenticated()) {
      return jwtService.generateToken(currentUser);
    } else return null;
  }
}
