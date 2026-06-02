package com.budget.buddy.budget_buddy_api.user.me.settings;

import com.budget.buddy.budget_buddy_api.base.config.MapstructConfig;
import com.budget.buddy.budget_buddy_contracts.generated.model.ClientSettings;
import com.budget.buddy.budget_buddy_contracts.generated.model.ClientSettingsWrite;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapstructConfig.class)
interface ClientSettingsMapper {

  ClientSettings toModel(UserClientSettingsEntity entity);

  List<ClientSettings> toModelList(List<UserClientSettingsEntity> entities);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "ownerId", ignore = true)
  @Mapping(target = "clientId", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateSettings(ClientSettingsWrite write, @MappingTarget UserClientSettingsEntity entity);
}
