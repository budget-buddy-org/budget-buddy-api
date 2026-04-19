package com.budget.buddy.budget_buddy_api.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository repository;

  @InjectMocks
  private UserService userService;

  @Nested
  class FindByOidcSubjectTests {

    @Test
    void should_ReturnUserDto_When_OidcSubjectExists() {
      var oidcSubject = "123456789";
      var userId = UUID.randomUUID();
      var entity = UserEntity.builder()
          .oidcSubject(oidcSubject)
          .username("testUser")
          .enabled(true)
          .build();
      entity.setId(userId);

      when(repository.findByOidcSubject(oidcSubject)).thenReturn(Optional.of(entity));

      var result = userService.findByOidcSubject(oidcSubject);

      assertThat(result).isPresent();
      assertThat(result.get().username()).isEqualTo("testUser");
    }

    @Test
    void should_ReturnEmpty_When_OidcSubjectNotFound() {
      when(repository.findByOidcSubject("nonexistent")).thenReturn(Optional.empty());

      var result = userService.findByOidcSubject("nonexistent");

      assertThat(result).isEmpty();
    }
  }

  @Nested
  class FindOrCreateByOidcSubjectTests {

    @Test
    void should_ReturnExistingUserId_When_UserExists() {
      var oidcSubject = "existing-sub";
      var userId = UUID.randomUUID();
      var entity = UserEntity.builder()
          .oidcSubject(oidcSubject)
          .username("existing")
          .enabled(true)
          .build();
      entity.setId(userId);

      when(repository.findByOidcSubject(oidcSubject)).thenReturn(Optional.of(entity));

      var jwt = Jwt.withTokenValue("token")
          .header("alg", "RS256")
          .subject(oidcSubject)
          .build();

      var result = userService.findOrCreateByOidcSubject(oidcSubject, jwt);

      assertThat(result).isEqualTo(userId);
    }

    @Test
    void should_CreateNewUser_When_UserDoesNotExist() {
      var oidcSubject = "new-sub";
      var savedId = UUID.randomUUID();
      var savedEntity = UserEntity.builder()
          .oidcSubject(oidcSubject)
          .username("johndoe")
          .enabled(true)
          .build();
      savedEntity.setId(savedId);

      when(repository.findByOidcSubject(oidcSubject)).thenReturn(Optional.empty());
      when(repository.save(any(UserEntity.class))).thenReturn(savedEntity);

      var jwt = Jwt.withTokenValue("token")
          .header("alg", "RS256")
          .subject(oidcSubject)
          .claim("preferred_username", "johndoe")
          .build();

      var result = userService.findOrCreateByOidcSubject(oidcSubject, jwt);

      assertThat(result).isEqualTo(savedId);
      verify(repository).save(any(UserEntity.class));
    }
  }

  @Nested
  class RequireEnabledUserTests {

    @Test
    void should_ReturnUserDto_When_UserIsEnabled() {
      var userId = UUID.randomUUID();
      var entity = UserEntity.builder()
          .oidcSubject("sub")
          .username("user")
          .enabled(true)
          .build();
      entity.setId(userId);

      when(repository.findById(userId)).thenReturn(Optional.of(entity));

      var result = userService.requireEnabledUser(userId);

      assertThat(result.id()).isEqualTo(userId);
      assertThat(result.username()).isEqualTo("user");
      assertThat(result.enabled()).isTrue();
    }

    @Test
    void should_ThrowException_When_UserIsDisabled() {
      var userId = UUID.randomUUID();
      var entity = UserEntity.builder()
          .oidcSubject("sub")
          .username("user")
          .enabled(false)
          .build();

      when(repository.findById(userId)).thenReturn(Optional.of(entity));

      assertThatThrownBy(() -> userService.requireEnabledUser(userId))
          .isInstanceOf(DisabledException.class)
          .hasMessage("User is disabled");
    }

    @Test
    void should_ThrowException_When_UserNotFound() {
      var userId = UUID.randomUUID();

      when(repository.findById(userId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> userService.requireEnabledUser(userId))
          .isInstanceOf(EntityNotFoundException.class);
    }
  }
}
