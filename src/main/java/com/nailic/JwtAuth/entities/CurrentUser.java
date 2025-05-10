package com.nailic.JwtAuth.entities;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "`current_user`")
public class CurrentUser implements UserDetails, Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String username;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;
  @ElementCollection(fetch = FetchType.EAGER)
  @Enumerated(EnumType.STRING)
  private Set<Role> roles;

  //otp attributes
  @Column(nullable = true)
  private String otp;
  @Column(nullable = true)
  private LocalDateTime otpExpiry;
  /**
   * @return
   */
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return roles.stream().map(role -> new SimpleGrantedAuthority(role.name()))
        .collect(Collectors.toList());
  }

  public Set<String> getRoleNames() {
    return roles.stream().map(Role::name).collect(Collectors.toSet());
  }

  /**
   * @return
   */
  @Override
  public String getPassword() {
    return this.password;
  }


  /**
   * @return
   */
  @Override
  public String getUsername() {
    return this.username;
  }

  /**
   * @return
   */
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  /**
   * @return
   */
  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  /**
   * @return
   */
  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  /**
   * @return
   */
  @Override
  public boolean isEnabled() {
    return true;
  }
}
