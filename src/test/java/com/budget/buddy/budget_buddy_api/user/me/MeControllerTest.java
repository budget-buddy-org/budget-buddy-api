package com.budget.buddy.budget_buddy_api.user.me;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budget.buddy.budget_buddy_api.user.me.preferences.UserPreferencesService;
import com.budget.buddy.budget_buddy_contracts.generated.model.ClientSettingsWrite;
import com.budget.buddy.budget_buddy_contracts.generated.model.UserPreferences;
import com.budget.buddy.budget_buddy_contracts.generated.model.UserPreferencesWrite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("MeController Unit Tests")
class MeControllerTest {

  @Mock
  private UserDataDeletionService deletionService;
  @Mock
  private UserPreferencesService preferencesService;

  private MeController controller;

  @BeforeEach
  void setUp() {
    controller = new MeController(deletionService, preferencesService);
  }

  @Nested
  class NotImplementedStubs {

    @Test
    void getCurrentUser_ShouldThrowUnsupportedOperationException() {
      assertThatThrownBy(() -> controller.getCurrentUser())
          .isInstanceOf(UnsupportedOperationException.class)
          .hasMessageContaining("getCurrentUser");
    }

    @Test
    void deleteCurrentUser_ShouldThrowUnsupportedOperationException() {
      assertThatThrownBy(() -> controller.deleteCurrentUser())
          .isInstanceOf(UnsupportedOperationException.class)
          .hasMessageContaining("deleteCurrentUser");
    }

    @Test
    void listCurrentUserClientSettings_ShouldThrowUnsupportedOperationException() {
      assertThatThrownBy(() -> controller.listCurrentUserClientSettings())
          .isInstanceOf(UnsupportedOperationException.class)
          .hasMessageContaining("listCurrentUserClientSettings");
    }

    @Test
    void getCurrentUserClientSettings_ShouldThrowUnsupportedOperationException() {
      assertThatThrownBy(() -> controller.getCurrentUserClientSettings("app"))
          .isInstanceOf(UnsupportedOperationException.class)
          .hasMessageContaining("getCurrentUserClientSettings");
    }

    @Test
    void upsertCurrentUserClientSettings_ShouldThrowUnsupportedOperationException() {
      var body = new ClientSettingsWrite();
      assertThatThrownBy(() -> controller.upsertCurrentUserClientSettings("app", body))
          .isInstanceOf(UnsupportedOperationException.class)
          .hasMessageContaining("upsertCurrentUserClientSettings");
    }

    @Test
    void deleteCurrentUserClientSettings_ShouldThrowUnsupportedOperationException() {
      assertThatThrownBy(() -> controller.deleteCurrentUserClientSettings("app"))
          .isInstanceOf(UnsupportedOperationException.class)
          .hasMessageContaining("deleteCurrentUserClientSettings");
    }
  }

  @Nested
  class ClearCurrentUserData {

    @Test
    void should_DelegateToServiceAndReturn204() {
      var response = controller.clearCurrentUserData();

      verify(deletionService).deleteUserData();
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
  }

  @Nested
  class GetCurrentUserPreferences {

    @Test
    void should_ReturnPreferencesFrom200() {
      var prefs = new UserPreferences().language("en");
      when(preferencesService.get()).thenReturn(prefs);

      var response = controller.getCurrentUserPreferences();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isSameAs(prefs);
    }
  }

  @Nested
  class UpdateCurrentUserPreferences {

    @Test
    void should_DelegateUpdateAndReturn200() {
      var write = new UserPreferencesWrite().language("de").currency("EUR").timezone("Europe/Berlin");
      var updated = new UserPreferences().language("de");
      when(preferencesService.update(write)).thenReturn(updated);

      var response = controller.updateCurrentUserPreferences(write);

      verify(preferencesService).update(write);
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isSameAs(updated);
    }
  }
}
