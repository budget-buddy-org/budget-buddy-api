package com.budget.buddy.budget_buddy_api.user;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Authority entity operations using Spring Data JDBC. Manages user roles and authorities for Spring Security.
 */
@Repository
public interface AuthorityRepository extends CrudRepository<AuthorityEntity, UUID> {

  /**
   * Find all authorities for a specific username.
   *
   * @param username the username
   * @return list of authorities for the user
   */
  List<AuthorityEntity> findAllByUsername(String username);

  boolean existsByUsernameAndAuthority(String username, String authority);
}
