package com.budget.buddy.budget_buddy_api.user.me.preferences;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntityService;
import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnerIdProvider;
import com.budget.buddy.budget_buddy_contracts.generated.model.UserPreferences;
import com.budget.buddy.budget_buddy_contracts.generated.model.UserPreferencesWrite;
import java.util.Collections;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPreferencesService
    extends OwnableEntityService<UserPreferencesEntity, UUID, UserPreferencesWrite, UserPreferences, UserPreferencesWrite, UserPreferences> {

  private final UserPreferencesDefaultsProvider defaultsProvider;

  public UserPreferencesService(
      UserPreferencesRepository repository,
      UserPreferencesMapper mapper,
      OwnerIdProvider<UUID> ownerIdProvider,
      UserPreferencesDefaultsProvider defaultsProvider
  ) {
    super(repository, mapper, Collections.emptySet(), ownerIdProvider);
    this.defaultsProvider = defaultsProvider;
  }

  @Override
  protected UserPreferencesRepository getRepository() {
    return (UserPreferencesRepository) super.getRepository();
  }

  public UserPreferences get() {
    return getRepository()
        .findByOwnerId(getOwnerIdProvider().get())
        .map(getMapper()::toModel)
        .orElseGet(defaultsProvider::get);
  }

  @Transactional
  public UserPreferences update(UserPreferencesWrite write) {
    var ownerId = getOwnerIdProvider().get();

    var entity = getRepository()
        .findByOwnerId(ownerId)
        .orElseGet(() -> {
          var fresh = getMapper().toEntity(write);
          fresh.setOwnerId(ownerId);
          return fresh;
        });

    getMapper().updateEntity(write, entity);
    return getMapper().toModel(getRepository().save(entity));
  }
}
