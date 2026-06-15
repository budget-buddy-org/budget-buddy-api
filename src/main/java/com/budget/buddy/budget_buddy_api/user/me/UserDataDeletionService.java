package com.budget.buddy.budget_buddy_api.user.me;

import com.budget.buddy.budget_buddy_api.category.CategoryService;
import com.budget.buddy.budget_buddy_api.transaction.TransactionService;
import com.budget.buddy.budget_buddy_api.user.me.preferences.UserPreferencesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Clears all data owned by the current user without removing the user account itself.
 *
 * <p>Deletion order is explicit and foreign-key-aware: {@code transactions.category_id} references
 * {@code categories} via a non-cascading FK, so transactions must be deleted before categories.
 * Each delete is scoped to the current owner by the underlying {@code OwnableEntityService}.
 * When a new ownable feature is added, add an explicit delete call here in FK-safe order.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserDataDeletionService {

  private final TransactionService transactionService;
  private final CategoryService categoryService;
  private final UserPreferencesService userPreferencesService;

  public void deleteUserData() {
    transactionService.deleteAllByOwnerId();
    categoryService.deleteAllByOwnerId();
    userPreferencesService.deleteAllByOwnerId();
  }
}
