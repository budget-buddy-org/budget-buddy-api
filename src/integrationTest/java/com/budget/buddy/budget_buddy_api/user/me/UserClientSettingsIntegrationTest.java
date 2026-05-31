package com.budget.buddy.budget_buddy_api.user.me;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.BaseMvcIntegrationTest;
import com.budget.buddy.budget_buddy_contracts.generated.model.ClientSettings;
import com.budget.buddy.budget_buddy_contracts.generated.model.ClientSettingsWrite;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class UserClientSettingsIntegrationTest extends BaseMvcIntegrationTest {

  private static final String BASE = "/v1/users/me/settings";

  private String userId;
  private String otherUserId;

  // ── helpers ────────────────────────────────────────────────────────────────

  private void putSettings(String subject, String clientId, Map<String, Object> settings) throws Exception {
    mvc.put().uri(BASE + "/{clientId}", clientId)
        .with(jwtForUser(subject))
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(new ClientSettingsWrite().settings(settings)))
        .exchange();
  }

  @BeforeEach
  void setUp() {
    userId = createTestUser();
    otherUserId = createTestUser();
  }

  @Nested
  class Upsert {

    @Test
    void should_StoreAndReturnSettings() throws Exception {
      var result = mvc.put().uri(BASE + "/{clientId}", "web")
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new ClientSettingsWrite().settings(Map.of("theme", "dark"))))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      assertThat(parseBody(result, ClientSettings.class))
          .returns("web", ClientSettings::getClientId)
          .returns(Map.of("theme", "dark"), ClientSettings::getSettings);
    }

    @Test
    void should_ReplaceOnSecondCall() throws Exception {
      putSettings(userId, "web", Map.of("theme", "dark"));
      putSettings(userId, "web", Map.of("theme", "light", "density", "compact"));

      var result = mvc.get().uri(BASE + "/{clientId}", "web").with(jwtForUser(userId)).exchange();
      assertThat(parseBody(result, ClientSettings.class).getSettings())
          .isEqualTo(Map.of("theme", "light", "density", "compact"));
    }

    @Test
    void should_Return400_When_ClientIdMalformed() throws Exception {
      var result = mvc.put().uri(BASE + "/{clientId}", "WEB_BAD")
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new ClientSettingsWrite().settings(Map.of("theme", "dark"))))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_Return401_When_NotAuthenticated() {
      var result = mvc.put().uri(BASE + "/{clientId}", "web")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new ClientSettingsWrite().settings(Map.of("theme", "dark"))))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class Get {

    @Test
    void should_ReturnStoredSettings() throws Exception {
      putSettings(userId, "web", Map.of("theme", "dark"));

      var result = mvc.get().uri(BASE + "/{clientId}", "web").with(jwtForUser(userId)).exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      assertThat(parseBody(result, ClientSettings.class).getClientId()).isEqualTo("web");
    }

    @Test
    void should_Return404_When_NotStored() {
      var result = mvc.get().uri(BASE + "/{clientId}", "web").with(jwtForUser(userId)).exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return404_When_BelongsToOtherUser() throws Exception {
      putSettings(otherUserId, "web", Map.of("theme", "dark"));

      var result = mvc.get().uri(BASE + "/{clientId}", "web").with(jwtForUser(userId)).exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return401_When_NotAuthenticated() {
      var result = mvc.get().uri(BASE + "/{clientId}", "web").exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class ListSettings {

    @Test
    void should_ReturnOwnRowsOldestFirst() throws Exception {
      putSettings(userId, "web", Map.of("theme", "dark"));
      putSettings(userId, "mobile-ios", Map.of("push", true));
      putSettings(otherUserId, "telegram-bot", Map.of("muted", true));

      var result = mvc.get().uri(BASE).with(jwtForUser(userId)).exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      assertThat(parseBody(result, ClientSettings[].class))
          .extracting(ClientSettings::getClientId)
          .as("only the caller's rows, oldest first")
          .containsExactly("web", "mobile-ios");
    }

    @Test
    void should_ReturnEmpty_When_NoneStored() throws Exception {
      var result = mvc.get().uri(BASE).with(jwtForUser(userId)).exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      assertThat(parseBody(result, ClientSettings[].class)).isEmpty();
    }

    @Test
    void should_Return401_When_NotAuthenticated() {
      var result = mvc.get().uri(BASE).exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class Delete {

    @Test
    void should_DeleteThenReturn404OnRead() throws Exception {
      putSettings(userId, "web", Map.of("theme", "dark"));

      var deleteResult = mvc.delete().uri(BASE + "/{clientId}", "web").with(jwtForUser(userId)).exchange();
      assertThat(deleteResult).hasStatus(HttpStatus.NO_CONTENT);

      var getResult = mvc.get().uri(BASE + "/{clientId}", "web").with(jwtForUser(userId)).exchange();
      assertThat(getResult).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return404_When_NotStored() {
      var result = mvc.delete().uri(BASE + "/{clientId}", "web").with(jwtForUser(userId)).exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return404_When_BelongsToOtherUser() throws Exception {
      putSettings(otherUserId, "web", Map.of("theme", "dark"));

      var result = mvc.delete().uri(BASE + "/{clientId}", "web").with(jwtForUser(userId)).exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return401_When_NotAuthenticated() {
      var result = mvc.delete().uri(BASE + "/{clientId}", "web").exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }
}
