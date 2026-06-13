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
    return new UserPreferencesWrite().language("en").currency("EUR").timezone("Europe/Tallinn");
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
      var result = mvc.get().uri(URI)
          .header("Accept-Language", "en-US")
          .with(jwtForUser(userId))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var body = parseBody(result, UserPreferences.class);
      assertThat(body.getLanguage()).isEqualTo("en");
      assertThat(body.getCurrency()).isEqualTo("USD");
    }

    @Test
    void should_ReturnNullDefaults_When_NeverStoredAndNoHeaders() throws Exception {
      var result = mvc.get().uri(URI).with(jwtForUser(userId)).exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var body = parseBody(result, UserPreferences.class);
      assertThat(body.getLanguage()).as("no headers → no derived defaults").isNull();
      assertThat(body.getCurrency()).isNull();
      assertThat(body.getTimezone()).isNull();
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
      var result = mvc.put().uri(URI)
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(validPreferences()))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      assertThat(parseBody(result, UserPreferences.class))
          .returns("en", UserPreferences::getLanguage)
          .returns("EUR", UserPreferences::getCurrency)
          .returns("Europe/Tallinn", UserPreferences::getTimezone);
    }

    @Test
    void should_PersistAcrossReads() throws Exception {
      mvc.put().uri(URI)
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(validPreferences()))
          .exchange();

      var result = mvc.get().uri(URI).with(jwtForUser(userId)).exchange();

      assertThat(parseBody(result, UserPreferences.class))
          .returns("en", UserPreferences::getLanguage)
          .returns("EUR", UserPreferences::getCurrency);
    }

    @Test
    void should_ReplaceOnSecondCall() throws Exception {
      mvc.put().uri(URI).with(jwtForUser(userId)).contentType(MediaType.APPLICATION_JSON)
          .content(json(validPreferences())).exchange();

      mvc.put().uri(URI).with(jwtForUser(userId)).contentType(MediaType.APPLICATION_JSON)
          .content(json(new UserPreferencesWrite().language("de").currency("USD").timezone("Europe/Berlin")))
          .exchange();

      var result = mvc.get().uri(URI).with(jwtForUser(userId)).exchange();
      assertThat(parseBody(result, UserPreferences.class))
          .returns("de", UserPreferences::getLanguage)
          .returns("USD", UserPreferences::getCurrency)
          .returns("Europe/Berlin", UserPreferences::getTimezone);
    }

    @Test
    void should_Return400_When_CurrencyMalformed() throws Exception {
      var result = mvc.put().uri(URI)
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new UserPreferencesWrite().language("en").currency("euro").timezone("Europe/Tallinn")))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_NotLeakToOtherUser() throws Exception {
      mvc.put().uri(URI).with(jwtForUser(userId)).contentType(MediaType.APPLICATION_JSON)
          .content(json(validPreferences())).exchange();

      var result = mvc.get().uri(URI).with(jwtForUser(otherUserId)).exchange();

      assertThat(parseBody(result, UserPreferences.class).getCurrency())
          .as("another user's preferences are not visible")
          .isNull();
    }

    @Test
    void should_Return401_When_NotAuthenticated() {
      var result = mvc.put().uri(URI)
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(validPreferences()))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }
}
