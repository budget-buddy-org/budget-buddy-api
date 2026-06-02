package com.budget.buddy.budget_buddy_api.user.me.preferences;

import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

interface UserPreferencesRepository extends CrudRepository<UserPreferencesEntity, UUID> {
}
