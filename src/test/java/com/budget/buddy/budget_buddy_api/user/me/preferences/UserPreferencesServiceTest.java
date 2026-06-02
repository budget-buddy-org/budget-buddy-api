package com.budget.buddy.budget_buddy_api.user.me.preferences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
  @Mock
  private UserPreferencesMapper mapper;

  private final UUID userId = UUID.randomUUID();

  private UserPreferencesService service;

  @BeforeEach
  void setUp() {
    service = new UserPreferencesService(repository, mapper, () -> userId);
  }

  @Nested
  @DisplayName("get")
  class Get {

    @Test
    void should_ReturnMappedPreferences_When_RowExists() {
      // Given
      var entity = new UserPreferencesEntity();
      var model = new UserPreferences();
      when(repository.findById(userId)).thenReturn(Optional.of(entity));
      when(mapper.toModel(entity)).thenReturn(model);

      // When & Then
      assertThat(service.get()).isSameAs(model);
    }

    @Test
    void should_ReturnUnsetDefaults_When_NoRowExists() {
      // Given
      when(repository.findById(userId)).thenReturn(Optional.empty());

      // When
      var result = service.get();

      // Then
      assertThat(result)
          .as("absent preferences map to an empty object, not an error")
          .returns(null, UserPreferences::getLanguage)
          .returns(null, UserPreferences::getCurrency)
          .returns(null, UserPreferences::getTimezone);
      verifyNoInteractions(mapper);
    }
  }

  @Nested
  @DisplayName("update")
  class Update {

    @Test
    void should_CreateRowScopedToCurrentUser_When_NoneExists() {
      // Given
      var write = new UserPreferencesWrite().language("de").currency("USD").timezone("Europe/Berlin");
      var model = new UserPreferences();
      when(repository.findById(userId)).thenReturn(Optional.empty());
      when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
      when(mapper.toModel(any())).thenReturn(model);

      // When
      var result = service.update(write);

      // Then
      var entityCaptor = ArgumentCaptor.forClass(UserPreferencesEntity.class);
      verify(mapper).updateEntity(eq(write), entityCaptor.capture());
      assertThat(entityCaptor.getValue().getUserId())
          .as("a freshly created preferences row is keyed to the current user")
          .isEqualTo(userId);
      verify(repository).save(entityCaptor.getValue());
      assertThat(result).isSameAs(model);
    }

    @Test
    void should_UpdateExistingRow_When_AlreadyStored() {
      // Given
      var write = new UserPreferencesWrite().language("de").currency("USD").timezone("Europe/Berlin");
      var existing = new UserPreferencesEntity();
      existing.setUserId(userId);
      when(repository.findById(userId)).thenReturn(Optional.of(existing));
      when(repository.save(existing)).thenReturn(existing);
      when(mapper.toModel(existing)).thenReturn(new UserPreferences());

      // When
      service.update(write);

      // Then
      verify(mapper).updateEntity(write, existing);
      verify(repository).save(existing);
    }
  }
}
