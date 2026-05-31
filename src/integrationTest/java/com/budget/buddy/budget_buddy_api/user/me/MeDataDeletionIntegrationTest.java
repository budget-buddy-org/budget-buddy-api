package com.budget.buddy.budget_buddy_api.user.me;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.BaseMvcIntegrationTest;
import com.budget.buddy.budget_buddy_contracts.generated.model.Category;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategoryWrite;
import com.budget.buddy.budget_buddy_contracts.generated.model.PaginatedCategories;
import com.budget.buddy.budget_buddy_contracts.generated.model.PaginatedTransactions;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionType;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionWrite;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class MeDataDeletionIntegrationTest extends BaseMvcIntegrationTest {

  private static final String DATA_URI = "/v1/users/me/data";

  private String userId;
  private String otherUserId;

  // ── helpers ────────────────────────────────────────────────────────────────

  private UUID createCategory(String ownerId, String name) throws Exception {
    var result = mvc.post().uri("/v1/categories")
        .with(jwtForUser(ownerId))
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(new CategoryWrite().name(name)))
        .exchange();

    return parseBody(result, Category.class).getId();
  }

  private void createTransaction(String ownerId, UUID categoryId) throws Exception {
    mvc.post().uri("/v1/transactions")
        .with(jwtForUser(ownerId))
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(new TransactionWrite()
            .categoryId(categoryId)
            .amount(1000L)
            .type(TransactionType.EXPENSE)
            .currency("EUR")
            .date(LocalDate.of(2026, 3, 1))))
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

    // Each user owns a category with a transaction that references it via a non-cascading FK,
    // so deletion must remove transactions before categories.
    createTransaction(userId, createCategory(userId, "Food"));
    createTransaction(otherUserId, createCategory(otherUserId, "Other Food"));
  }

  // ── tests ──────────────────────────────────────────────────────────────────

  @Nested
  class ClearData {

    @Test
    void should_ClearCallersCategoriesAndTransactions() throws Exception {
      var result = mvc.delete().uri(DATA_URI).with(jwtForUser(userId)).exchange();

      assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
      assertThat(transactionCount(userId)).as("caller's transactions are cleared").isZero();
      assertThat(categoryCount(userId)).as("caller's categories are cleared").isZero();
    }

    @Test
    void should_LeaveDataIntact_When_BelongsToOtherUser() throws Exception {
      mvc.delete().uri(DATA_URI).with(jwtForUser(userId)).exchange();

      assertThat(transactionCount(otherUserId)).as("other user's transactions are untouched").isEqualTo(1);
      assertThat(categoryCount(otherUserId)).as("other user's categories are untouched").isEqualTo(1);
    }

    @Test
    void should_ReturnNoContent_When_AlreadyEmpty() throws Exception {
      mvc.delete().uri(DATA_URI).with(jwtForUser(userId)).exchange();

      var second = mvc.delete().uri(DATA_URI).with(jwtForUser(userId)).exchange();

      assertThat(second).as("clearing an already-empty account is idempotent").hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    void should_Return401_When_NotAuthenticated() {
      var result = mvc.delete().uri(DATA_URI).exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }
}
