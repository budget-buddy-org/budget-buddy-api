package com.budget.buddy.budget_buddy_api.user.me.preferences;

import com.budget.buddy.budget_buddy_api.base.config.MapstructConfig;
import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntityMapper;
import com.budget.buddy.budget_buddy_api.base.mapper.CurrencyMapper;
import com.budget.buddy.budget_buddy_contracts.generated.model.UserPreferences;
import com.budget.buddy.budget_buddy_contracts.generated.model.UserPreferencesWrite;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

@Mapper(config = MapstructConfig.class, uses = CurrencyMapper.class)
public interface UserPreferencesMapper
    extends OwnableEntityMapper<UserPreferencesEntity, UserPreferencesWrite, UserPreferences, UserPreferencesWrite, UserPreferences> {

  @Override
  default UserPreferences toPage(Page<UserPreferencesEntity> page) {
    throw new UnsupportedOperationException("UserPreferences is a singleton resource — paged list is not supported");
  }

}
