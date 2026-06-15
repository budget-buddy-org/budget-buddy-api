package com.budget.buddy.budget_buddy_api.user.me;

import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.budget.buddy.budget_buddy_api.category.CategoryService;
import com.budget.buddy.budget_buddy_api.transaction.TransactionService;
import com.budget.buddy.budget_buddy_api.user.me.preferences.UserPreferencesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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

    var inOrder = Mockito.inOrder(transactionService, categoryService, userPreferencesService);
    inOrder.verify(transactionService).deleteAllByOwnerId();
    inOrder.verify(categoryService).deleteAllByOwnerId();
    inOrder.verify(userPreferencesService).deleteAllByOwnerId();
    verifyNoMoreInteractions(transactionService, categoryService, userPreferencesService);
  }
}
