package com.budget.buddy.budget_buddy_api.user.me.preferences;

import com.budget.buddy.budget_buddy_api.base.config.MapstructConfig;
import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntityMapper;
import com.budget.buddy.budget_buddy_contracts.generated.model.UserPreferences;
import com.budget.buddy.budget_buddy_contracts.generated.model.UserPreferencesWrite;
import java.util.Currency;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

@Mapper(config = MapstructConfig.class)
public interface UserPreferencesMapper
    extends OwnableEntityMapper<UserPreferencesEntity, UserPreferencesWrite, UserPreferences, UserPreferencesWrite, UserPreferences> {

  @Override
  default UserPreferences toPage(Page<UserPreferencesEntity> page) {
    throw new UnsupportedOperationException("UserPreferences is a singleton resource — paged list is not supported");
  }

  default @Nullable Currency toCurrency(@Nullable String code) {
    return code == null ? null : Currency.getInstance(code);
  }

  default @Nullable String fromCurrency(@Nullable Currency currency) {
    return currency == null ? null : currency.getCurrencyCode();
  }
}
