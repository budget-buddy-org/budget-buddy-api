package com.budget.buddy.budget_buddy_api.user.me.preferences;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnerIdProvider;
import com.budget.buddy.budget_buddy_contracts.generated.model.UserPreferences;
import com.budget.buddy.budget_buddy_contracts.generated.model.UserPreferencesWrite;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads and replaces the authenticated user's global preferences. All operations are scoped to the
 * current user via {@link OwnerIdProvider}.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserPreferencesService {

  private final UserPreferencesRepository repository;
  private final OwnerIdProvider<UUID> ownerIdProvider;

  /**
   * Returns the current user's preferences, or unset server defaults when none have been stored.
   */
  public UserPreferences get() {
    return repository.findByUserId(ownerIdProvider.get())
        .map(UserPreferencesService::toModel)
        .orElseGet(UserPreferences::new);
  }

  /**
   * Fully replaces the current user's preferences, creating the row on first call (PUT semantics).
   */
  @Transactional
  public UserPreferences update(UserPreferencesWrite write) {
    var saved = repository.upsert(
        ownerIdProvider.get(),
        new UserPreferencesRow(write.getLanguage(), write.getCurrency(), write.getTimezone()));
    return toModel(saved);
  }

  private static UserPreferences toModel(UserPreferencesRow row) {
    return new UserPreferences()
        .language(row.language())
        .currency(row.currency())
        .timezone(row.timezone());
  }
}
