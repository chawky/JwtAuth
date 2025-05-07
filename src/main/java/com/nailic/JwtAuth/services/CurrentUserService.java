package com.nailic.JwtAuth.services;

import com.nailic.JwtAuth.DTOs.AuthResponse;
import com.nailic.JwtAuth.DTOs.CurrentUserDto;
import com.nailic.JwtAuth.entities.CurrentUser;
import com.nailic.JwtAuth.entities.RefreshToken;
import com.nailic.JwtAuth.entities.Role;
import com.nailic.JwtAuth.exceptions.NotFoundException;
import com.nailic.JwtAuth.repos.CurrentUserRepo;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
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
  @Autowired
  private CurrentUserRepo currentUserRepo;
  @Autowired
  private JwtService jwtService;
  @Autowired
  private RefreshTokenService refreshTokenService;

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

  public CurrentUser registerUser(CurrentUserDto userDto) {
    ModelMapper userMapper = new ModelMapper();
    Converter<Set<String>, Set<Role>> roleConverter =
        mappingContext -> {
          Set<String> source = mappingContext.getSource();
          if (source == null) {
            return Collections.emptySet();
          }
          return source.stream()
              .map(Role::valueOf)
              .collect(Collectors.toSet());
        };
    userMapper.typeMap(CurrentUserDto.class, CurrentUser.class)
        .addMappings(m ->
            m.using(roleConverter)
                .map(CurrentUserDto::getRoles, CurrentUser::setRoles));
    CurrentUser user = userMapper.map(userDto, CurrentUser.class);
    if (currentUserRepo.findByUsername(user.getUsername())!=null) {
      throw new RuntimeException("User already registered. Please use different username.");
    }
    user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
    return currentUserRepo.save(user);
  }

  public AuthResponse login(CurrentUser currentUser, AuthenticationManager authenticationManager)
      throws NotFoundException {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                currentUser.getUsername(), currentUser.getPassword()));
    if (authentication.isAuthenticated()) {
      RefreshToken refreshToken = refreshTokenService.createRefreshToken(
          ((CurrentUser)authentication.getPrincipal()).getId());
      String token = jwtService.generateToken(currentUser);
      return AuthResponse.builder()
          .refreshToken(refreshToken.getToken())
          .userId(((CurrentUser)authentication.getPrincipal()).getId())
          .roles(currentUser.getRoles() != null ? currentUser.getRoleNames() : null)
          .token(token).build();

    } else {
      return null;
    }
  }

}
