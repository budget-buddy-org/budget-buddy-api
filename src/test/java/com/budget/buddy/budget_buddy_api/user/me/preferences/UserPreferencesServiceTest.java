package com.budget.buddy.budget_buddy_api.user.me.preferences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budget.buddy.budget_buddy_contracts.generated.model.UserPreferences;
import com.budget.buddy.budget_buddy_contracts.generated.model.UserPreferencesWrite;
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
@DisplayName("UserPreferencesService Unit Tests")
class UserPreferencesServiceTest {

  @Mock
  private UserPreferencesRepository repository;

  private final UUID userId = UUID.randomUUID();

  private UserPreferencesService service;

  @BeforeEach
  void setUp() {
    service = new UserPreferencesService(repository, () -> userId);
  }

  @Nested
  @DisplayName("get")
  class Get {

    @Test
    void should_ReturnStoredPreferences_When_RowExists() {
      // Given
      when(repository.findByUserId(userId))
          .thenReturn(Optional.of(new UserPreferencesRow("en", "EUR", "Europe/Tallinn")));

      // When
      var result = service.get();

      // Then
      assertThat(result)
          .returns("en", UserPreferences::getLanguage)
          .returns("EUR", UserPreferences::getCurrency)
          .returns("Europe/Tallinn", UserPreferences::getTimezone);
    }

    @Test
    void should_ReturnUnsetDefaults_When_NoRowExists() {
      // Given
      when(repository.findByUserId(userId)).thenReturn(Optional.empty());

      // When
      var result = service.get();

      // Then
      assertThat(result)
          .as("absent preferences map to an empty object, not an error")
          .returns(null, UserPreferences::getLanguage)
          .returns(null, UserPreferences::getCurrency)
          .returns(null, UserPreferences::getTimezone);
    }
  }

  @Nested
  @DisplayName("update")
  class Update {

    @Test
    void should_UpsertScopedToCurrentUser_AndReturnSaved() {
      // Given
      var write = new UserPreferencesWrite().language("de").currency("USD").timezone("Europe/Berlin");
      when(repository.upsert(eq(userId), any()))
          .thenReturn(new UserPreferencesRow("de", "USD", "Europe/Berlin"));

      // When
      var result = service.update(write);

      // Then
      var rowCaptor = ArgumentCaptor.forClass(UserPreferencesRow.class);
      verify(repository).upsert(eq(userId), rowCaptor.capture());
      assertThat(rowCaptor.getValue())
          .as("write fields are forwarded verbatim to the persistence row")
          .isEqualTo(new UserPreferencesRow("de", "USD", "Europe/Berlin"));
      assertThat(result)
          .returns("de", UserPreferences::getLanguage)
          .returns("USD", UserPreferences::getCurrency)
          .returns("Europe/Berlin", UserPreferences::getTimezone);
    }
  }
}
