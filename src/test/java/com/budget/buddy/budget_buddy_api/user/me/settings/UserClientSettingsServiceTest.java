package com.budget.buddy.budget_buddy_api.user.me.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import com.budget.buddy.budget_buddy_contracts.generated.model.ClientSettings;
import com.budget.buddy.budget_buddy_contracts.generated.model.ClientSettingsWrite;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserClientSettingsService Unit Tests")
class UserClientSettingsServiceTest {

  @Mock
  private UserClientSettingsRepository repository;
  @Mock
  private ClientSettingsMapper mapper;

  private final UUID ownerId = UUID.randomUUID();

  private UserClientSettingsService service;

  @BeforeEach
  void setUp() {
    service = new UserClientSettingsService(repository, mapper, () -> ownerId);
  }

  @Nested
  @DisplayName("get")
  class Get {

    @Test
    void should_ReturnMappedSettings_When_Found() {
      // Given
      var entity = new UserClientSettingsEntity();
      var model = new ClientSettings();
      when(repository.findByOwnerIdAndClientId(ownerId, "web")).thenReturn(Optional.of(entity));
      when(mapper.toModel(entity)).thenReturn(model);

      // When & Then
      assertThat(service.get("web")).isSameAs(model);
    }

    @Test
    void should_ThrowEntityNotFound_When_MissingOrBelongsToOtherUser() {
      // Given
      when(repository.findByOwnerIdAndClientId(ownerId, "web")).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> service.get("web"))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("web");
    }
  }

  @Nested
  @DisplayName("list")
  class ListSettings {

    @Test
    void should_ReturnMappedRowsForCurrentUser() {
      // Given
      var entities = List.of(new UserClientSettingsEntity());
      var models = List.of(new ClientSettings().clientId("web"));
      when(repository.findByOwnerIdOrderByCreatedAt(ownerId)).thenReturn(entities);
      when(mapper.toModelList(entities)).thenReturn(models);

      // When & Then
      assertThat(service.list()).isSameAs(models);
    }
  }

  @Nested
  @DisplayName("upsert")
  class Upsert {

    @Test
    void should_CreateRowScopedToCurrentUser_When_NoneExists() {
      // Given
      var write = new ClientSettingsWrite().settings(Map.of("theme", "dark"));
      var model = new ClientSettings();
      when(repository.findByOwnerIdAndClientId(ownerId, "web")).thenReturn(Optional.empty());
      when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
      when(mapper.toModel(any())).thenReturn(model);

      // When
      var result = service.upsert("web", write);

      // Then
      var entityCaptor = ArgumentCaptor.forClass(UserClientSettingsEntity.class);
      verify(mapper).updateSettings(eq(write), entityCaptor.capture());
      assertThat(entityCaptor.getValue())
          .as("a freshly created row is keyed to the current user and requested client")
          .returns(ownerId, UserClientSettingsEntity::getOwnerId)
          .returns("web", UserClientSettingsEntity::getClientId);
      verify(repository).save(entityCaptor.getValue());
      assertThat(result).isSameAs(model);
    }

    @Test
    void should_UpdateExistingRow_When_AlreadyStored() {
      // Given
      var write = new ClientSettingsWrite().settings(Map.of("theme", "light"));
      var existing = new UserClientSettingsEntity();
      when(repository.findByOwnerIdAndClientId(ownerId, "web")).thenReturn(Optional.of(existing));
      when(repository.save(existing)).thenReturn(existing);
      when(mapper.toModel(existing)).thenReturn(new ClientSettings());

      // When
      service.upsert("web", write);

      // Then
      verify(mapper).updateSettings(write, existing);
      verify(repository).save(existing);
    }
  }

  @Nested
  @DisplayName("delete")
  class Delete {

    @Test
    void should_Delete_When_RowExists() {
      // Given
      var existing = new UserClientSettingsEntity();
      when(repository.findByOwnerIdAndClientId(ownerId, "web")).thenReturn(Optional.of(existing));

      // When
      service.delete("web");

      // Then
      verify(repository).delete(existing);
    }

    @Test
    void should_ThrowEntityNotFound_When_Missing() {
      // Given
      when(repository.findByOwnerIdAndClientId(ownerId, "web")).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> service.delete("web"))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("web");
    }
  }
}
