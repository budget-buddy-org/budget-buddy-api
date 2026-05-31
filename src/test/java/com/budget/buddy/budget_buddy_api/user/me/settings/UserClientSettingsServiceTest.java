package com.budget.buddy.budget_buddy_api.user.me.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserClientSettingsService Unit Tests")
class UserClientSettingsServiceTest {

  @Mock
  private UserClientSettingsRepository repository;

  private final UUID userId = UUID.randomUUID();

  private UserClientSettingsService service;

  @BeforeEach
  void setUp() {
    service = new UserClientSettingsService(repository, () -> userId);
  }

  @Nested
  @DisplayName("get")
  class Get {

    @Test
    void should_ReturnSettings_When_Found() {
      // Given
      when(repository.findByUserIdAndClientId(userId, "web"))
          .thenReturn(Optional.of(new ClientSettingsRow("web", Map.of("theme", "dark"))));

      // When
      var result = service.get("web");

      // Then
      assertThat(result)
          .returns("web", ClientSettings::getClientId)
          .returns(Map.of("theme", "dark"), ClientSettings::getSettings);
    }

    @Test
    void should_ThrowEntityNotFound_When_BelongsToOtherUserOrMissing() {
      // Given
      when(repository.findByUserIdAndClientId(userId, "web")).thenReturn(Optional.empty());

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
    void should_ReturnAllRowsForCurrentUser() {
      // Given
      when(repository.findAllByUserId(userId)).thenReturn(List.of(
          new ClientSettingsRow("web", Map.of("theme", "dark")),
          new ClientSettingsRow("mobile-ios", Map.of("push", true))));

      // When
      var result = service.list();

      // Then
      assertThat(result)
          .extracting(ClientSettings::getClientId)
          .containsExactly("web", "mobile-ios");
    }
  }

  @Nested
  @DisplayName("upsert")
  class Upsert {

    @Test
    void should_StoreScopedToCurrentUser_AndReturnSaved() {
      // Given
      var settings = Map.<String, Object>of("theme", "dark");
      var write = new ClientSettingsWrite().settings(settings);
      when(repository.upsert(userId, "web", settings))
          .thenReturn(new ClientSettingsRow("web", settings));

      // When
      var result = service.upsert("web", write);

      // Then
      verify(repository).upsert(userId, "web", settings);
      assertThat(result)
          .returns("web", ClientSettings::getClientId)
          .returns(settings, ClientSettings::getSettings);
    }
  }

  @Nested
  @DisplayName("delete")
  class Delete {

    @Test
    void should_Delete_When_RowExists() {
      // Given
      when(repository.deleteByUserIdAndClientId(userId, "web")).thenReturn(1);

      // When & Then
      service.delete("web");
      verify(repository).deleteByUserIdAndClientId(userId, "web");
    }

    @Test
    void should_ThrowEntityNotFound_When_NothingDeleted() {
      // Given
      when(repository.deleteByUserIdAndClientId(userId, "web")).thenReturn(0);

      // When & Then
      assertThatThrownBy(() -> service.delete("web"))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("web");
    }
  }
}
