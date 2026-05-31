package com.budget.buddy.budget_buddy_api.user.me;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntityService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Clears all data owned by the current user without removing the user account itself.
 *
 * <p>The {@link OwnableEntityService} beans are injected as an ordered {@link List} — Spring honours
 * {@link org.springframework.core.annotation.Order @Order} on each service — so entities are deleted
 * in foreign-key-safe order (referencing rows before the rows they reference, e.g. transactions
 * before categories). Plain {@link java.util.Set} injection would not guarantee this ordering.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserDataDeletionService {

  private final List<? extends OwnableEntityService<?, ?, ?, ?, ?>> services;

  public void deleteUserData() {
    services.forEach(OwnableEntityService::deleteAllByOwnerId);
  }
}
