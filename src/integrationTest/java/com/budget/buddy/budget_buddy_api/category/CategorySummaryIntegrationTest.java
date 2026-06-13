package com.budget.buddy.budget_buddy_api.category;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.BaseMvcIntegrationTest;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategorySpendingRow;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategorySpendingSummary;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategoryWrite;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionType;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionWrite;
import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class CategorySummaryIntegrationTest extends BaseMvcIntegrationTest {

  private String userId;
  private String otherUserId;

  // ── helpers ────────────────────────────────────────────────────────────────

  private UUID createCategory(String ownerId, String name) throws Exception {
    return createCategory(ownerId, name, null);
  }

  private UUID createCategory(String ownerId, String name, Long monthlyBudget) throws Exception {
    var body = new CategoryWrite().name(name).monthlyBudget(monthlyBudget);
    var result = mvc.post().uri("/v1/categories")
        .with(jwtForUser(ownerId))
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(body))
        .exchange();
    return parseBody(result, com.budget.buddy.budget_buddy_contracts.generated.model.Category.class).getId();
  }

  private void createTransaction(
      String ownerId, UUID categoryId, long amount, TransactionType type, String currency, LocalDate date
  ) {
    var body = new TransactionWrite()
        .categoryId(categoryId)
        .amount(amount)
        .type(type)
        .currency(currency)
        .date(date);
    mvc.post().uri("/v1/transactions")
        .with(jwtForUser(ownerId))
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(body))
        .exchange();
  }

  private CategorySpendingSummary getSummary(String ownerId) throws Exception {
    var result = mvc.get().uri("/v1/categories/summary?month={m}&currency={c}", "2026-03", "EUR")
        .with(jwtForUser(ownerId))
        .exchange();
    assertThat(result).hasStatus(HttpStatus.OK);
    return parseBody(result, CategorySpendingSummary.class);
  }

  @BeforeEach
  void setUp() {
    userId = createTestUser();
    otherUserId = createTestUser();
  }

  // ── tests ──────────────────────────────────────────────────────────────────

  @Nested
  class EmptyMonth {

    @Test
    void should_ReturnAllCategories_WithZeros_When_NoTransactions() throws Exception {
      var catId = createCategory(userId, "Groceries");

      var summary = getSummary(userId);

      assertThat(summary.getMonth()).isEqualTo("2026-03");
      assertThat(summary.getCurrency()).isEqualTo("EUR");
      assertThat(summary.getItems()).hasSize(1);
      var row = summary.getItems().getFirst();
      assertThat(row.getCategoryId()).isEqualTo(catId);
      assertThat(row.getCategoryName()).isEqualTo("Groceries");
      assertThat(row.getSpent()).isZero();
      assertThat(row.getTransactionCount()).isZero();
      assertThat(row.getExcludedTransactionCount()).isZero();
    }
  }

  @Nested
  class ExpenseVsIncome {

    @Test
    void should_SumOnlyExpenses_When_MixedTypes() throws Exception {
      var catId = createCategory(userId, "Food");
      createTransaction(userId, catId, 1000L, TransactionType.EXPENSE, "EUR", LocalDate.of(2026, Month.MARCH, 15));
      createTransaction(userId, catId, 5000L, TransactionType.INCOME, "EUR", LocalDate.of(2026, Month.MARCH, 15));

      var summary = getSummary(userId);

      var row = rowForCategory(summary, catId);
      assertThat(row.getSpent()).isEqualTo(1000L);
      assertThat(row.getTransactionCount()).isEqualTo(1);
    }
  }

  @Nested
  class MixedCurrencies {

    @Test
    void should_SumMatchingCurrency_And_CountOthersAsExcluded() throws Exception {
      var catId = createCategory(userId, "Travel");
      createTransaction(userId, catId, 2000L, TransactionType.EXPENSE, "EUR", LocalDate.of(2026, Month.MARCH, 10));
      createTransaction(userId, catId, 3000L, TransactionType.EXPENSE, "USD", LocalDate.of(2026, Month.MARCH, 10));
      createTransaction(userId, catId, 500L, TransactionType.EXPENSE, "USD", LocalDate.of(2026, Month.MARCH, 11));

      var summary = getSummary(userId);

      var row = rowForCategory(summary, catId);
      assertThat(row.getSpent()).isEqualTo(2000L);
      assertThat(row.getTransactionCount()).isEqualTo(1);
      assertThat(row.getExcludedTransactionCount()).isEqualTo(2);
    }
  }

  @Nested
  class MonthlyBudget {

    @Test
    void should_ReturnNullBudget_When_NoBudgetSet() throws Exception {
      var catId = createCategory(userId, "NoLimit");

      var summary = getSummary(userId);

      var row = rowForCategory(summary, catId);
      assertThat(row.getMonthlyBudget()).isNull();
    }

    @Test
    void should_ReturnZeroBudget_When_BudgetIsZero() throws Exception {
      var catId = createCategory(userId, "ZeroLimit", 0L);

      var summary = getSummary(userId);

      var row = rowForCategory(summary, catId);
      assertThat(row.getMonthlyBudget()).isZero();
    }

    @Test
    void should_ReturnBudget_When_BudgetIsSet() throws Exception {
      var catId = createCategory(userId, "Groceries", 50000L);

      var summary = getSummary(userId);

      var row = rowForCategory(summary, catId);
      assertThat(row.getMonthlyBudget()).isEqualTo(50000L);
    }
  }

  @Nested
  class BoundaryDates {

    @Test
    void should_IncludeBoundaryTransactions_When_OnFirstAndLastDayOfMonth() throws Exception {
      var catId = createCategory(userId, "Bills");
      createTransaction(userId, catId, 100L, TransactionType.EXPENSE, "EUR", LocalDate.of(2026, Month.MARCH, 1));
      createTransaction(userId, catId, 200L, TransactionType.EXPENSE, "EUR", LocalDate.of(2026, Month.MARCH, 31));
      createTransaction(userId, catId, 999L, TransactionType.EXPENSE, "EUR", LocalDate.of(2026, Month.FEBRUARY, 28));
      createTransaction(userId, catId, 999L, TransactionType.EXPENSE, "EUR", LocalDate.of(2026, Month.APRIL, 1));

      var summary = getSummary(userId);

      var row = rowForCategory(summary, catId);
      assertThat(row.getSpent()).isEqualTo(300L);
      assertThat(row.getTransactionCount()).isEqualTo(2);
    }
  }

  @Nested
  class OwnerIsolation {

    @Test
    void should_NotReturnOtherUsersCategories() throws Exception {
      createCategory(otherUserId, "Other's category");
      var myCategory = createCategory(userId, "Mine");

      var summary = getSummary(userId);

      assertThat(summary.getItems()).hasSize(1);
      assertThat(summary.getItems().getFirst().getCategoryId()).isEqualTo(myCategory);
    }

    @Test
    void should_NotCountOtherUsersTransactions() throws Exception {
      var catId = createCategory(userId, "Shared name");
      var otherCatId = createCategory(otherUserId, "Shared name");
      createTransaction(userId, catId, 1000L, TransactionType.EXPENSE, "EUR", LocalDate.of(2026, Month.MARCH, 10));
      createTransaction(otherUserId, otherCatId, 9999L, TransactionType.EXPENSE, "EUR", LocalDate.of(2026, Month.MARCH, 10));

      var summary = getSummary(userId);

      var row = rowForCategory(summary, catId);
      assertThat(row.getSpent()).isEqualTo(1000L);
    }
  }

  @Nested
  class Validation {

    @Test
    void should_Return401_When_NotAuthenticated() {
      var result = mvc.get().uri("/v1/categories/summary?month=2026-03&currency=EUR")
          .exchange();
      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validationArguments")
    void should_Return400_When_InvalidRequest(String uriTemplate) {
      var result = mvc.get().uri(uriTemplate)
          .with(jwtForUser(userId))
          .exchange();
      assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    static Stream<Named<String>> validationArguments() {
      return Stream.of(
          Named.of("Invalid Month format", "/v1/categories/summary?month=2026-13&currency=EUR"),
          Named.of("Missing Month", "/v1/categories/summary?currency=EUR"),
          Named.of("Currency too long", "/v1/categories/summary?month=2026-03&currency=EURO"),
          Named.of("Missing Currency", "/v1/categories/summary?month=2026-03")
      );
    }
  }

  // ── private helpers ────────────────────────────────────────────────────────

  private CategorySpendingRow rowForCategory(CategorySpendingSummary summary, UUID categoryId) {
    return summary.getItems().stream()
        .filter(r -> categoryId.equals(r.getCategoryId()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Category " + categoryId + " not found in summary"));
  }

}
