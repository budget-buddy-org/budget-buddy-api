package com.budget.buddy.budget_buddy_api.user;

import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing users.
 * Users are provisioned automatically via JIT provisioning on first OIDC login.
 */
@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository repository;

  /**
   * Finds a user by OIDC subject (JWT sub claim).
   *
   * @param oidcSubject the OIDC subject identifier
   * @return an {@link Optional} containing the user, or empty if not found
   */
  public Optional<UserDto> findByOidcSubject(String oidcSubject) {
    return repository.findByOidcSubject(oidcSubject)
        .map(UserService::toDto);
  }

  /**
   * Finds or creates a local user for the given OIDC subject.
   * On first login, a new user is provisioned automatically (JIT provisioning).
   *
   * @param oidcSubject the OIDC subject identifier (JWT sub claim)
   * @param jwt the JWT token, used to extract display name for new users
   * @return the local user's UUID
   */
  @Transactional
  public UUID findOrCreateByOidcSubject(String oidcSubject, Jwt jwt) {
    return repository.findByOidcSubject(oidcSubject)
        .map(UserEntity::getId)
        .orElseGet(() -> {
          log.info("Provisioning new user for OIDC subject: {}", oidcSubject);
          var user = UserEntity.builder()
              .oidcSubject(oidcSubject)
              .username(resolveUsername(jwt))
              .enabled(true)
              .build();
          return repository.save(user).getId();
        });
  }

  /**
   * Find and validate that user exists and is enabled.
   *
   * @param userId user ID
   * @return UserDto if user exists and is enabled
   * @throws EntityNotFoundException if user does not exist
   * @throws DisabledException if user is disabled
   */
  public UserDto requireEnabledUser(UUID userId) {
    var user = repository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    if (!user.isEnabled()) {
      throw new DisabledException("User is disabled");
    }
    return toDto(user);
  }

  private static UserDto toDto(UserEntity entity) {
    return new UserDto(entity.getId(), entity.getUsername(), entity.isEnabled());
  }

  private static String resolveUsername(Jwt jwt) {
    var preferred = jwt.getClaimAsString("preferred_username");
    if (preferred != null && !preferred.isBlank()) {
      return preferred;
    }
    var email = jwt.getClaimAsString("email");
    if (email != null && !email.isBlank()) {
      return email;
    }
    return jwt.getSubject();
  }
}
