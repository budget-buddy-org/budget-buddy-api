package com.budget.buddy.budget_buddy_api.user.me.preferences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserPreferencesDefaultsProvider Unit Tests")
class UserPreferencesDefaultsProviderTest {

  @Mock
  private HttpServletRequest request;

  private UserPreferencesDefaultsProvider provider;

  @BeforeEach
  void setUp() {
    provider = new UserPreferencesDefaultsProvider(request);
  }

  @Nested
  class Get {

    @Test
    void should_ReturnNullPreferences_When_NoAcceptLanguageHeader() {
      when(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn(null);

      var result = provider.get();

      assertThat(result.getLanguage()).isNull();
      assertThat(result.getCurrency()).isNull();
    }

    @Test
    void should_ReturnNullPreferences_When_AcceptLanguageIsMalformed() {
      // Syntactically invalid language tag causes Locale.LanguageRange.parse to throw
      // IllegalArgumentException — the catch block returns Optional.empty()
      when(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn("not a valid@@tag!!");

      var result = provider.get();

      assertThat(result.getLanguage()).isNull();
      assertThat(result.getCurrency()).isNull();
    }

    @Test
    void should_ReturnLanguageWithoutCurrency_When_LocaleHasNoCountry() {
      // "zh" resolves to Locale.CHINESE which has no country — currency lookup fails silently
      when(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn("zh");

      var result = provider.get();

      assertThat(result.getLanguage()).isEqualTo("zh");
      assertThat(result.getCurrency()).isNull();
    }

    @Test
    void should_ReturnLanguageAndCurrency_When_LocaleHasCountry() {
      when(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn("en-US");

      var result = provider.get();

      assertThat(result.getLanguage()).isEqualTo("en");
      assertThat(result.getCurrency()).isEqualTo("USD");
    }
  }
}
