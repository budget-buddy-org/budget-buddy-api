package com.budget.buddy.budget_buddy_api.base.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CurrencyMapper Unit Tests")
class CurrencyMapperTest {

  private final CurrencyMapper mapper = new CurrencyMapperImpl();

  @Nested
  class ToCurrency {

    @Test
    void should_ReturnNull_When_CodeIsNull() {
      assertThat(mapper.toCurrency(null)).isNull();
    }

    @Test
    void should_ReturnCurrency_When_CodeIsValid() {
      assertThat(mapper.toCurrency("EUR")).isEqualTo(Currency.getInstance("EUR"));
    }
  }

  @Nested
  class ToCurrencyCode {

    @Test
    void should_ReturnNull_When_CurrencyIsNull() {
      assertThat(mapper.toCurrencyCode(null)).isNull();
    }

    @Test
    void should_ReturnCode_When_CurrencyIsNotNull() {
      assertThat(mapper.toCurrencyCode(Currency.getInstance("USD"))).isEqualTo("USD");
    }
  }
}
