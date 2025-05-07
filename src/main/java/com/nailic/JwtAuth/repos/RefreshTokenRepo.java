package com.nailic.JwtAuth.repos;

import com.nailic.JwtAuth.entities.CurrentUser;
import com.nailic.JwtAuth.entities.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByUser(CurrentUser currentUser);

  Optional<RefreshToken> findByToken(String refreshToken);
}
