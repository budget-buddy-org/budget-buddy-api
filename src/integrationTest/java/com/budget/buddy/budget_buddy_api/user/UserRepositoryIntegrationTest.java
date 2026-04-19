package com.budget.buddy.budget_buddy_api.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.BaseIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("UserRepository Integration Tests")
class UserRepositoryIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("should save and find user by username")
  void shouldSaveAndFindByUsername() {
    // Given
    var username = "testuser_" + UUID.randomUUID();
    var user = UserEntity.builder()
        .username(username)
        .oidcSubject("sub_" + UUID.randomUUID())
        .enabled(true)
        .build();

    // When
    userRepository.save(user);
    var found = userRepository.findByUsername(username);

    // Then
    assertThat(found)
        .isPresent()
        .get()
        .returns(username, UserEntity::getUsername);
  }

  @Test
  @DisplayName("should check if user exists by username")
  void shouldCheckExistsByUsername() {
    // Given
    var username = "exists_" + UUID.randomUUID();
    var user = UserEntity.builder()
        .username(username)
        .oidcSubject("sub_" + UUID.randomUUID())
        .enabled(true)
        .build();
    userRepository.save(user);

    // When
    boolean exists = userRepository.existsByUsername(username);
    boolean notExists = userRepository.existsByUsername("nonexistent");

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("should find user by ID")
  void shouldFindById() {
    // Given
    var user = UserEntity.builder()
        .username("id_user_" + UUID.randomUUID())
        .oidcSubject("sub_" + UUID.randomUUID())
        .enabled(true)
        .build();
    var saved = userRepository.save(user);
    assertThat(saved.getId()).isNotNull();

    // When
    var found = userRepository.findById(saved.getId());

    // Then
    assertThat(found)
        .isPresent()
        .get()
        .returns(saved.getId(), UserEntity::getId);
  }

  @Test
  @DisplayName("should find user by OIDC subject")
  void shouldFindByOidcSubject() {
    // Given
    var oidcSubject = "oidc_" + UUID.randomUUID();
    var user = UserEntity.builder()
        .username("oidc_user_" + UUID.randomUUID())
        .oidcSubject(oidcSubject)
        .enabled(true)
        .build();
    userRepository.save(user);

    // When
    var found = userRepository.findByOidcSubject(oidcSubject);
    var notFound = userRepository.findByOidcSubject("nonexistent");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getOidcSubject()).isEqualTo(oidcSubject);
    assertThat(notFound).isEmpty();
  }
}
