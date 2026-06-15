package com.budget.buddy.budget_buddy_api.user.me;

import com.budget.buddy.budget_buddy_api.category.CategoryService;
import com.budget.buddy.budget_buddy_api.transaction.TransactionService;
import com.budget.buddy.budget_buddy_api.user.me.preferences.UserPreferencesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class UserDataDeletionServiceTest {

  @Mock
  private TransactionService transactionService;

  @Mock
  private CategoryService categoryService;

  @Mock
  private UserPreferencesService userPreferencesService;

  @InjectMocks
  private UserDataDeletionService userDataDeletionService;

  @Test
  void deleteUserData_Should_DeleteInForeignKeySafeOrder() {
    userDataDeletionService.deleteUserData();

    var inOrder = org.mockito.Mockito.inOrder(transactionService, categoryService, userPreferencesService);
    inOrder.verify(transactionService).deleteAllByOwnerId();
    inOrder.verify(categoryService).deleteAllByOwnerId();
    inOrder.verify(userPreferencesService).deleteAllByOwnerId();
    verifyNoMoreInteractions(transactionService, categoryService, userPreferencesService);
  }
}
