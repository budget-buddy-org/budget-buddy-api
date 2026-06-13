package com.budget.buddy.budget_buddy_api.user.me.preferences;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntityRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserPreferencesRepository extends OwnableEntityRepository<UserPreferencesEntity, UUID> {

  Optional<UserPreferencesEntity> findByOwnerId(UUID ownerId);
}
