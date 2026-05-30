package com.budget.buddy.budget_buddy_api.base.mapper;

import com.budget.buddy.budget_buddy_api.base.config.MapstructConfig;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;

import java.util.Currency;
import java.util.Optional;

@Mapper(config = MapstructConfig.class)
public interface CurrencyMapper {

  default @Nullable Currency toCurrency(@Nullable String code) {
    return Optional.ofNullable(code)
        .map(Currency::getInstance)
        .orElse(null);
  }

  default @Nullable String toCurrencyCode(@Nullable Currency currency) {
    return Optional.ofNullable(currency)
        .map(Currency::getCurrencyCode)
        .orElse(null);
  }

}
