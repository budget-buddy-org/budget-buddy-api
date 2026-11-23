package com.budget.buddy.budget_buddy_api.user.me.preferences;

import com.budget.buddy.budget_buddy_contracts.generated.model.UserPreferences;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * Derives best-effort user preferences from the current HTTP request when no stored row exists.
 * Language and currency come from {@code Accept-Language}.
 */
@Component
@RequiredArgsConstructor
public class UserPreferencesDefaultsProvider {

  private static final List<Locale> AVAILABLE_LOCALES = List.of(Locale.getAvailableLocales());
  private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
  private static final Currency DEFAULT_CURRENCY = Currency.getInstance(Locale.US);
  private static final String DEFAULT_TIMEZONE = "UTC";

  private final HttpServletRequest request;

  public UserPreferences get() {
    return fromLocale(resolveLocale().orElse(DEFAULT_LOCALE));
  }

  private UserPreferences fromLocale(Locale locale) {
    var language = locale.getLanguage();
    var currency =
        resolveCurrency(locale)
            .map(Currency::getCurrencyCode)
            .orElse(DEFAULT_CURRENCY.getCurrencyCode());
    return new UserPreferences(language, currency, DEFAULT_TIMEZONE);
  }

  private Optional<Locale> resolveLocale() {
    try {
      return Optional.ofNullable(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE))
          .filter(Predicate.not(String::isBlank))
          .map(header -> {
            var ranges = Locale.LanguageRange.parse(header);
            return Locale.lookup(ranges, AVAILABLE_LOCALES);
          });
    } catch (IllegalArgumentException _) {
      return Optional.empty();
    }
  }

  private Optional<Currency> resolveCurrency(Locale locale) {
    try {
      return Optional.of(Currency.getInstance(locale));
    } catch (IllegalArgumentException _) {
      return Optional.empty();
    }
  }
}
