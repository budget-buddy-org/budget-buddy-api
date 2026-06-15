package com.budget.buddy.budget_buddy_api.user.me;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.BaseMvcIntegrationTest;
import com.budget.buddy.budget_buddy_contracts.generated.model.Category;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategoryWrite;
import com.budget.buddy.budget_buddy_contracts.generated.model.PaginatedCategories;
import com.budget.buddy.budget_buddy_contracts.generated.model.PaginatedTransactions;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionType;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionWrite;
import com.budget.buddy.budget_buddy_contracts.generated.model.UserPreferences;
import com.budget.buddy.budget_buddy_contracts.generated.model.UserPreferencesWrite;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class MeDeleteCurrentUserIntegrationTest extends BaseMvcIntegrationTest {

  private static final String ME_URI = "/v1/users/me";
  private static final String PREFERENCES_URI = "/v1/users/me/preferences";

  private String userId;
  private String otherUserId;

  private UUID createCategory(String ownerId, String name) throws Exception {
    var result = mvc.post().uri("/v1/categories")
        .with(jwtForUser(ownerId))
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(new CategoryWrite().name(name)))
        .exchange();
    return parseBody(result, Category.class).getId();
  }

  private void createTransaction(String ownerId, UUID categoryId) {
    mvc.post().uri("/v1/transactions")
        .with(jwtForUser(ownerId))
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(new TransactionWrite()
            .categoryId(categoryId)
            .amount(500L)
            .type(TransactionType.EXPENSE)
            .currency("EUR")
            .date(LocalDate.of(2026, 1, 1))))
        .exchange();
  }

  private void storePreferences(String ownerId) {
    mvc.put().uri(PREFERENCES_URI)
        .with(jwtForUser(ownerId))
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(new UserPreferencesWrite().language("en").currency("EUR").timezone("Europe/Tallinn")))
        .exchange();
  }

  private long categoryCount(String ownerId) throws Exception {
    var result = mvc.get().uri("/v1/categories").with(jwtForUser(ownerId)).exchange();
    return parseBody(result, PaginatedCategories.class).getMeta().getTotal();
  }

  private long transactionCount(String ownerId) throws Exception {
    var result = mvc.get().uri("/v1/transactions").with(jwtForUser(ownerId)).exchange();
    return parseBody(result, PaginatedTransactions.class).getMeta().getTotal();
  }

  @BeforeEach
  void setUp() throws Exception {
    userId = createTestUser();
    otherUserId = createTestUser();

    var categoryId = createCategory(userId, "Groceries");
    createTransaction(userId, categoryId);
    storePreferences(userId);

    var otherCategoryId = createCategory(otherUserId, "Travel");
    createTransaction(otherUserId, otherCategoryId);
    storePreferences(otherUserId);
  }

  @Nested
  class DeleteCurrentUser {

    @Test
    void should_Return204() {
      var result = mvc.delete().uri(ME_URI).with(jwtForUser(userId)).exchange();

      assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    void should_CascadeDeleteAllUserData() throws Exception {
      mvc.delete().uri(ME_URI).with(jwtForUser(userId)).exchange();

      // The deleted user's JWT re-provisions a fresh account — expect empty data
      assertThat(transactionCount(userId)).as("transactions are gone").isZero();
      assertThat(categoryCount(userId)).as("categories are gone").isZero();
      var prefs = parseBody(
          mvc.get().uri(PREFERENCES_URI).with(jwtForUser(userId)).exchange(),
          UserPreferences.class);
      assertThat(prefs.getCurrency()).as("preferences are gone").isNull();
    }

    @Test
    void should_LeaveOtherUsersDataIntact() throws Exception {
      mvc.delete().uri(ME_URI).with(jwtForUser(userId)).exchange();

      assertThat(transactionCount(otherUserId)).as("other user's transactions untouched").isEqualTo(1);
      assertThat(categoryCount(otherUserId)).as("other user's categories untouched").isEqualTo(1);
      var prefs = parseBody(
          mvc.get().uri(PREFERENCES_URI).with(jwtForUser(otherUserId)).exchange(),
          UserPreferences.class);
      assertThat(prefs.getCurrency()).as("other user's preferences untouched").isEqualTo("EUR");
    }

    @Test
    void should_Return401_When_NotAuthenticated() {
      var result = mvc.delete().uri(ME_URI).exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }
}
