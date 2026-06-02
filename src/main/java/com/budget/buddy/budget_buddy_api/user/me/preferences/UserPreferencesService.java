package com.budget.buddy.budget_buddy_api.user.me.preferences;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnerIdProvider;
import com.budget.buddy.budget_buddy_contracts.generated.model.UserPreferences;
import com.budget.buddy.budget_buddy_contracts.generated.model.UserPreferencesWrite;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads and replaces the authenticated user's global preferences, scoped to the current user via
 * {@link OwnerIdProvider}.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserPreferencesService {

  private final UserPreferencesRepository repository;
  private final UserPreferencesMapper mapper;
  private final OwnerIdProvider<UUID> ownerIdProvider;

  /**
   * Returns the current user's preferences, or unset server defaults when none have been stored.
   */
  public UserPreferences get() {
    return repository.findById(ownerIdProvider.get())
        .map(mapper::toModel)
        .orElseGet(UserPreferences::new);
  }

  /**
   * Fully replaces the current user's preferences, creating the row on first call (PUT semantics).
   */
  @Transactional
  public UserPreferences update(UserPreferencesWrite write) {
    var userId = ownerIdProvider.get();
    var entity = repository.findById(userId).orElseGet(() -> {
      var fresh = new UserPreferencesEntity();
      fresh.setUserId(userId);
      return fresh;
    });
    mapper.updateEntity(write, entity);
    return mapper.toModel(repository.save(entity));
  }
}
