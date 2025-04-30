package com.nailic.JwtAuth.repos;

import com.nailic.JwtAuth.entities.CurrentUser;
import com.nailic.JwtAuth.exceptions.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrentUserRepo extends JpaRepository<CurrentUser, Long> {
  CurrentUser findByUsername(String username);

  CurrentUser findByEmail(String email) throws NotFoundException;

  CurrentUser findByUsernameAndPassword(String username, String password) throws NotFoundException;
}
