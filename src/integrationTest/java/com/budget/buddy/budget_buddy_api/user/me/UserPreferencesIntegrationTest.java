package com.budget.buddy.budget_buddy_api.user.me;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.BaseMvcIntegrationTest;
import com.budget.buddy.budget_buddy_contracts.generated.model.UserPreferences;
import com.budget.buddy.budget_buddy_contracts.generated.model.UserPreferencesWrite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class UserPreferencesIntegrationTest extends BaseMvcIntegrationTest {

  private static final String URI = "/v1/users/me/preferences";

  private String userId;
  private String otherUserId;

  private UserPreferencesWrite validPreferences() {
    return new UserPreferencesWrite("en", "EUR", "Europe/Tallinn");
  }

  @BeforeEach
  void setUp() {
    userId = createTestUser();
    otherUserId = createTestUser();
  }

  @Nested
  class Get {

    @Test
    void should_ReturnHttpDerivedDefaults_When_NeverStored() throws Exception {
      var result =
          mvc.get().uri(URI).header("Accept-Language", "en-US").with(jwtForUser(userId)).exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var body = parseBody(result, UserPreferences.class);
      assertThat(body.language()).isEqualTo("en");
      assertThat(body.currency()).isEqualTo("USD");
    }

    @Test
    void should_ReturnNullDefaults_When_NeverStoredAndNoHeaders() throws Exception {
      var result = mvc.get().uri(URI).with(jwtForUser(userId)).exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var body = parseBody(result, UserPreferences.class);
      assertThat(body.language()).as("no headers → no derived defaults").isNull();
      assertThat(body.currency()).isNull();
      assertThat(body.timezone()).isNull();
    }

    @Test
    void should_Return401_When_NotAuthenticated() {
      var result = mvc.get().uri(URI).exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class Update {

    @Test
    void should_StoreAndReturnPreferences() throws Exception {
      var result =
          mvc.put()
              .uri(URI)
              .with(jwtForUser(userId))
              .contentType(MediaType.APPLICATION_JSON)
              .content(json(validPreferences()))
              .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      assertThat(parseBody(result, UserPreferences.class))
          .returns("en", UserPreferences::language)
          .returns("EUR", UserPreferences::currency)
          .returns("Europe/Tallinn", UserPreferences::timezone);
    }

    @Test
    void should_PersistAcrossReads() throws Exception {
      mvc.put()
          .uri(URI)
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(validPreferences()))
          .exchange();

      var result = mvc.get().uri(URI).with(jwtForUser(userId)).exchange();

      assertThat(parseBody(result, UserPreferences.class))
          .returns("en", UserPreferences::language)
          .returns("EUR", UserPreferences::currency);
    }

    @Test
    void should_ReplaceOnSecondCall() throws Exception {
      mvc.put()
          .uri(URI)
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(validPreferences()))
          .exchange();

      mvc.put()
          .uri(URI)
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new UserPreferencesWrite("de", "USD", "Europe/Berlin")))
          .exchange();

      var result = mvc.get().uri(URI).with(jwtForUser(userId)).exchange();
      assertThat(parseBody(result, UserPreferences.class))
          .returns("de", UserPreferences::language)
          .returns("USD", UserPreferences::currency)
          .returns("Europe/Berlin", UserPreferences::timezone);
    }

    @Test
    void should_Return400_When_CurrencyMalformed() throws Exception {
      var result =
          mvc.put()
              .uri(URI)
              .with(jwtForUser(userId))
              .contentType(MediaType.APPLICATION_JSON)
              .content(json(new UserPreferencesWrite("en", "euro", "Europe/Tallinn")))
              .exchange();

      assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_NotLeakToOtherUser() throws Exception {
      mvc.put()
          .uri(URI)
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(validPreferences()))
          .exchange();

      var result = mvc.get().uri(URI).with(jwtForUser(otherUserId)).exchange();

      assertThat(parseBody(result, UserPreferences.class).currency())
          .as("another user's preferences are not visible")
          .isNull();
    }

    @Test
    void should_Return401_When_NotAuthenticated() {
      var result =
          mvc.put()
              .uri(URI)
              .contentType(MediaType.APPLICATION_JSON)
              .content(json(validPreferences()))
              .exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }
}
