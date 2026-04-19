package com.budget.buddy.budget_buddy_api.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.budget.buddy.budget_buddy_contracts.generated.model.RegisterRequest;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserMapperTest {

  private final UserMapper userMapper = new UserMapper();

  @Nested
  class ToEntity {

    @Test
    void should_MapRegisterRequestToUserEntity() {
      // Given
      var request = new RegisterRequest();
      request.setUsername("testuser");
      request.setPassword("password123");

      // When
      var entity = userMapper.toEntity(request);

      // Then
      assertThat(entity)
          .as("Mapped user entity should have correct values and be enabled by default")
          .isNotNull()
          .returns(request.getUsername(), UserEntity::getUsername)
          .returns(true, UserEntity::isEnabled);
    }
  }

  @Nested
  class ToModel {

    @Test
    void should_MapUserEntityToUserDto() {
      // Given
      var userId = UUID.randomUUID();
      var entity = UserEntity.builder()
          .id(userId)
          .username("testuser")
          .oidcSubject("12345")
          .enabled(true)
          .build();

      // When
      var dto = userMapper.toModel(entity);

      // Then
      assertThat(dto)
          .as("Mapped UserDto should match the entity values")
          .isNotNull()
          .returns(userId, UserDto::id)
          .returns(entity.getUsername(), UserDto::username)
          .returns(true, UserDto::enabled);
    }
  }

  @Nested
  class UnsupportedOperations {

    @Test
    void should_ThrowException_On_ToModelList() {
      assertThatThrownBy(() -> userMapper.toModelList(null))
          .as("toModelList operation should be unsupported for users")
          .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void should_ThrowException_On_ToPageResponse() {
      assertThatThrownBy(() -> userMapper.toPageResponse(null, null))
          .as("toPageResponse operation should be unsupported for users")
          .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void should_ThrowException_On_PatchEntity() {
      assertThatThrownBy(() -> userMapper.patchEntity(null, null))
          .as("patchEntity operation should be unsupported for users")
          .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void should_ThrowException_On_ReplaceEntity() {
      assertThatThrownBy(() -> userMapper.replaceEntity(null, null))
          .as("replaceEntity operation should be unsupported for users")
          .isInstanceOf(UnsupportedOperationException.class);
    }
  }
}
