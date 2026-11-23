package com.budget.buddy.budget_buddy_api.user.me.preferences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserPreferencesDefaultsProvider Unit Tests")
class UserPreferencesDefaultsProviderTest {

  @Mock private HttpServletRequest request;

  private UserPreferencesDefaultsProvider provider;

  @BeforeEach
  void setUp() {
    provider = new UserPreferencesDefaultsProvider(request);
  }

  @Nested
  class Get {

    @ParameterizedTest(name = "Accept-Language {0} yields {1} and {2}")
    @CsvSource({", en, USD", "not a valid@@tag!!, en, USD", "zh, zh, USD", "de-DE, de, EUR"})
    void should_DerivePreferencesFromAcceptLanguage(
        String header, String expectedLanguage, String expectedCurrency) {
      when(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn(header);

      var result = provider.get();

      assertThat(result.language()).isEqualTo(expectedLanguage);
      assertThat(result.currency()).isEqualTo(expectedCurrency);
      assertThat(result.timezone()).isEqualTo("UTC");
    }
  }
}
