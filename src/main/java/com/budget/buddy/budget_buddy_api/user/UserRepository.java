package com.budget.buddy.budget_buddy_api.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for User entity operations using Spring Data JDBC.
 */
@Repository
public interface UserRepository extends CrudRepository<UserEntity, UUID> {

  /**
   * Find user by username
   *
   * @param username user name
   * @return Optional containing user if found
   */
  Optional<UserEntity> findByUsername(String username);
}
