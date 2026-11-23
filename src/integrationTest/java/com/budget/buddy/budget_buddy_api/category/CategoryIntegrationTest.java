package com.budget.buddy.budget_buddy_api.category;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.BaseMvcIntegrationTest;
import com.budget.buddy.budget_buddy_contracts.generated.model.Category;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategoryWrite;
import com.budget.buddy.budget_buddy_contracts.generated.model.PaginatedCategories;
import com.budget.buddy.budget_buddy_contracts.generated.model.PaginationMeta;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class CategoryIntegrationTest extends BaseMvcIntegrationTest {

  private String userId;
  private String otherUserId;

  // ── helpers ────────────────────────────────────────────────────────────────

  Category createCategory(String ownerId, String name) throws Exception {
    return createCategory(ownerId, name, null);
  }

  Category createCategory(String ownerId, String name, Long monthlyBudget) throws Exception {
    var result =
        mvc.post()
            .uri("/v1/categories")
            .with(jwtForUser(ownerId))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(new CategoryWrite(name, monthlyBudget)))
            .exchange();

    return parseBody(result, Category.class);
  }

  @BeforeEach
  void setUp() {
    userId = createTestUser();
    otherUserId = createTestUser();
  }

  // ── tests ──────────────────────────────────────────────────────────────────

  @Nested
  class Create {

    @Test
    void should_CreateCategory_When_ValidRequest() throws Exception {
      var category = createCategory(userId, "Groceries");

      assertThat(category.id()).isNotNull();
      assertThat(category.name()).isEqualTo("Groceries");
    }

    @Test
    void should_Return401_When_NotAuthenticated() {
      var result =
          mvc.post()
              .uri("/v1/categories")
              .contentType(MediaType.APPLICATION_JSON)
              .content(json(new CategoryWrite("Groceries", null)))
              .exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_ReturnLocationHeader_When_Created() {
      var result =
          mvc.post()
              .uri("/v1/categories")
              .with(jwtForUser(userId))
              .contentType(MediaType.APPLICATION_JSON)
              .content(json(new CategoryWrite("Groceries", null)))
              .exchange();

      assertThat(result).hasStatus(HttpStatus.CREATED).containsHeader("Location");
    }
  }

  @Nested
  class Read {

    @Test
    void should_ReturnCategory_When_Owner() throws Exception {
      var created = createCategory(userId, "Food");

      var result =
          mvc.get().uri("/v1/categories/{id}", created.id()).with(jwtForUser(userId)).exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var category = parseBody(result, Category.class);
      assertThat(category.id()).isEqualTo(created.id());
      assertThat(category.name()).isEqualTo("Food");
    }

    @Test
    void should_Return404_When_CategoryBelongsToOtherUser() throws Exception {
      var created = createCategory(otherUserId, "Other's category");

      var result =
          mvc.get().uri("/v1/categories/{id}", created.id()).with(jwtForUser(userId)).exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return404_When_CategoryNotFound() {
      var result =
          mvc.get()
              .uri("/v1/categories/{id}", UUID.randomUUID())
              .with(jwtForUser(userId))
              .exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND).bodyJson().doesNotHavePath("$.errors");
    }

    @Test
    void should_Return401_When_NotAuthenticated() throws Exception {
      var created = createCategory(userId, "Food");

      var result = mvc.get().uri("/v1/categories/{id}", created.id()).exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class Update {

    @Test
    void should_UpdateCategory_When_Owner() throws Exception {
      var created = createCategory(userId, "Food");
      var newCategoryName = "Groceries";

      var result =
          mvc.put()
              .uri("/v1/categories/{id}", created.id())
              .with(jwtForUser(userId))
              .contentType(MediaType.APPLICATION_JSON)
              .content(json(new CategoryWrite(newCategoryName, null)))
              .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var updated = parseBody(result, Category.class);

      assertThat(updated)
          .returns(created.id(), (Category::id))
          .returns(newCategoryName, Category::name);
    }

    @Test
    void should_Return404_When_CategoryBelongsToOtherUser() throws Exception {
      var created = createCategory(otherUserId, "Other's category");

      var result =
          mvc.put()
              .uri("/v1/categories/{id}", created.id())
              .with(jwtForUser(userId))
              .contentType(MediaType.APPLICATION_JSON)
              .content(json(new CategoryWrite("Hacked", null)))
              .exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return401_When_NotAuthenticated() throws Exception {
      var created = createCategory(userId, "Food");

      var result =
          mvc.put()
              .uri("/v1/categories/{id}", created.id())
              .contentType(MediaType.APPLICATION_JSON)
              .content(json(new CategoryWrite("Hacked", null)))
              .exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class Delete {

    @Test
    void should_DeleteCategory_When_Owner() throws Exception {
      var created = createCategory(userId, "Food");

      var deleteResult =
          mvc.delete().uri("/v1/categories/{id}", created.id()).with(jwtForUser(userId)).exchange();

      assertThat(deleteResult).hasStatus(HttpStatus.NO_CONTENT);

      var getResult =
          mvc.get().uri("/v1/categories/{id}", created.id()).with(jwtForUser(userId)).exchange();

      assertThat(getResult).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return404_When_CategoryBelongsToOtherUser() throws Exception {
      var created = createCategory(otherUserId, "Other's category");

      var result =
          mvc.delete().uri("/v1/categories/{id}", created.id()).with(jwtForUser(userId)).exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return401_When_NotAuthenticated() throws Exception {
      var created = createCategory(userId, "Food");

      var result = mvc.delete().uri("/v1/categories/{id}", created.id()).exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class MonthlyBudget {

    @Test
    void should_ClearBudget_When_PutOmitsMonthlyBudget() throws Exception {
      var created = createCategory(userId, "Groceries", 50000L);

      var result =
          mvc.put()
              .uri("/v1/categories/{id}", created.id())
              .with(jwtForUser(userId))
              .contentType(MediaType.APPLICATION_JSON)
              .content(json(new CategoryWrite("Groceries", null)))
              .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var updated = parseBody(result, Category.class);
      assertThat(updated.monthlyBudget()).isNull();
    }

    @Test
    void should_UpdateBudget_When_PutSendsNewMonthlyBudget() throws Exception {
      var created = createCategory(userId, "Groceries", 50000L);

      var result =
          mvc.put()
              .uri("/v1/categories/{id}", created.id())
              .with(jwtForUser(userId))
              .contentType(MediaType.APPLICATION_JSON)
              .content(json(new CategoryWrite("Groceries", 12345L)))
              .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var updated = parseBody(result, Category.class);
      assertThat(updated.monthlyBudget()).isEqualTo(12345L);
    }
  }

  @Nested
  class List {

    @Test
    void should_ReturnOnlyOwnCategoriesOrderedByName() throws Exception {
      createCategory(userId, "My Transport");
      createCategory(userId, "My Food");
      createCategory(otherUserId, "Other's Food");

      var result = mvc.get().uri("/v1/categories").with(jwtForUser(userId)).exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var page = parseBody(result, PaginatedCategories.class);
      assertThat(page.items()).hasSize(2);
      assertThat(page.items())
          .extracting(Category::name)
          .containsExactly("My Food", "My Transport");
    }

    @Test
    void should_ReturnEmptyList_When_NoCategories() throws Exception {
      var result = mvc.get().uri("/v1/categories").with(jwtForUser(userId)).exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var page = parseBody(result, PaginatedCategories.class);
      assertThat(page.items()).isEmpty();
      assertThat(page.meta().total()).isZero();
    }

    @Test
    void should_ReturnPagedResults_When_MultiplePages() throws Exception {
      createCategory(userId, "A");
      createCategory(userId, "B");
      createCategory(userId, "C");
      createCategory(userId, "D");

      var result =
          mvc.get().uri("/v1/categories?page=1&size=2").with(jwtForUser(userId)).exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var page = parseBody(result, PaginatedCategories.class);
      assertThat(page.items()).hasSize(2);
      assertThat(page.meta())
          .returns(1, PaginationMeta::page)
          .returns(2, PaginationMeta::size)
          .returns(4L, PaginationMeta::total);
    }

    @Test
    void should_Return401_When_NotAuthenticated() {
      var result = mvc.get().uri("/v1/categories").exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }
}
