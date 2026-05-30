package com.budget.buddy.budget_buddy_api.base.mapper;

import com.budget.buddy.budget_buddy_api.base.config.MapstructConfig;
import java.util.Currency;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;

@Mapper(config = MapstructConfig.class)
public interface CurrencyMapper {

  default @Nullable Currency toCurrency(@Nullable String code) {
    if (code == null) {
      return null;
    }

    return Currency.getInstance(code);
  }

  default @Nullable String toCurrencyCode(@Nullable Currency currency) {
    if (currency == null) {
      return null;
    }

    return currency.getCurrencyCode();
  }

}
