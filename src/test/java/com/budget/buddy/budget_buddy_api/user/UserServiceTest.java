package com.budget.buddy.budget_buddy_api.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budget.buddy.budget_buddy_api.generated.model.RegisterRequest;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository repository;
  @Mock
  private UserMapper mapper;
  @Mock
  private AuthorityRepository authorityRepository;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService(repository, mapper, authorityRepository, Collections.emptySet());
  }

  @Nested
  class ExistsTests {

    @Test
    void should_ReturnTrue_When_UsernameExists() {
      // Given
      String username = "existingUser";
      when(repository.existsByUsername(username)).thenReturn(true);

      // When
      boolean exists = userService.existsByUsername(username);

      // Then
      assertThat(exists).isTrue();
      verify(repository).existsByUsername(username);
    }
  }

  @Nested
  class FindByUsernameTests {

    @Test
    void should_ReturnUserDto_When_UsernameExists() {
      // Given
      String username = "testUser";
      UserEntity entity = new UserEntity();
      UserDto dto = new UserDto(UUID.randomUUID(), username, true);

      when(repository.findByUsername(username)).thenReturn(Optional.of(entity));
      when(mapper.toModel(entity)).thenReturn(dto);

      // When
      Optional<UserDto> result = userService.findByUsername(username);

      // Then
      assertThat(result).contains(dto);
      verify(repository).findByUsername(username);
      verify(mapper).toModel(entity);
    }
  }

  @Nested
  class CreateInternalTests {

    @Test
    void should_CreateUserAndAddAuthority() {
      // Given
      RegisterRequest request = new RegisterRequest("newuser", "password");
      UserEntity entity = new UserEntity();
      entity.setUsername("newuser");

      when(repository.save(any(UserEntity.class))).thenReturn(entity);
      when(mapper.toEntity(request)).thenReturn(new UserEntity());

      // When
      UserEntity result = userService.createInternal(request);

      // Then
      assertThat(result).isNotNull();
      verify(repository).save(any(UserEntity.class));
      verify(authorityRepository).addDefaultAuthorityToUser("newuser");
    }
  }

  @Nested
  class RequireEnabledUserTests {

    @Test
    void should_ReturnUserDto_When_UserIsEnabled() {
      // Given
      UUID userId = UUID.randomUUID();
      UserEntity entity = new UserEntity();
      entity.setEnabled(true);
      UserDto dto = new UserDto(userId, "user", true);

      when(repository.findById(userId)).thenReturn(Optional.of(entity));
      when(mapper.toModel(entity)).thenReturn(dto);

      // When
      UserDto result = userService.requireEnabledUser(userId);

      // Then
      assertThat(result).isEqualTo(dto);
    }

    @Test
    void should_ThrowException_When_UserIsDisabled() {
      // Given
      UUID userId = UUID.randomUUID();
      UserEntity entity = new UserEntity();
      entity.setEnabled(false);

      when(repository.findById(userId)).thenReturn(Optional.of(entity));

      // When & Then
      assertThatThrownBy(() -> userService.requireEnabledUser(userId))
          .isInstanceOf(DisabledException.class)
          .hasMessage("User is disabled");
    }
  }

  @Nested
  class UnsupportedOperationsTests {

    @Test
    void should_ThrowException_On_UnsupportedOperations() {
      UUID id = UUID.randomUUID();
      assertThatThrownBy(() -> userService.update(id, new Object())).isInstanceOf(UnsupportedOperationException.class);
      assertThatThrownBy(() -> userService.delete(id)).isInstanceOf(UnsupportedOperationException.class);
      assertThatThrownBy(() -> userService.list()).isInstanceOf(UnsupportedOperationException.class);
      assertThatThrownBy(() -> userService.list(null)).isInstanceOf(UnsupportedOperationException.class);
      assertThatThrownBy(() -> userService.count()).isInstanceOf(UnsupportedOperationException.class);
    }
  }
}
