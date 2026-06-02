package com.budget.buddy.budget_buddy_api.user.me.settings;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;

interface UserClientSettingsRepository extends ListCrudRepository<UserClientSettingsEntity, UUID> {

  Optional<UserClientSettingsEntity> findByOwnerIdAndClientId(UUID ownerId, String clientId);

  List<UserClientSettingsEntity> findByOwnerIdOrderByCreatedAt(UUID ownerId);
}
