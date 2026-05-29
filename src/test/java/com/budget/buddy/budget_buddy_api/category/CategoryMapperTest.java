package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_contracts.generated.model.Category;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategoryWrite;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryMapperTest {

  private final CategoryMapper categoryMapper = new CategoryMapperImpl();

  @Nested
  class ToEntity {

    @Test
    void should_MapCategoryWriteToCategoryEntity() {
      // Given
      var create = new CategoryWrite()
          .name("Groceries")
          .monthlyBudget(50000L);

      // When
      var entity = categoryMapper.toEntity(create);

      // Then
      assertThat(entity)
          .as("Mapped entity should not be null")
          .isNotNull()
          .returns("Groceries", CategoryEntity::getName)
          .returns(50000L, CategoryEntity::getMonthlyBudget)
          .returns(null, CategoryEntity::getId);
    }

    @Test
    void should_MapNullMonthlyBudget_When_Cleared() {
      // Given
      var create = new CategoryWrite()
          .name("Groceries");

      // When
      var entity = categoryMapper.toEntity(create);

      // Then
      assertThat(entity.getMonthlyBudget())
          .as("Null monthlyBudget in the write model should map to null on the entity")
          .isNull();
    }
  }

  @Nested
  class ToModel {

    @Test
    void should_MapCategoryEntityToCategory() {
      // Given
      var id = UUID.randomUUID();
      var entity = new CategoryEntity(id, "Groceries", UUID.randomUUID(), 50000L);

      // When
      var model = categoryMapper.toModel(entity);

      // Then
      assertThat(model)
          .as("Mapped model should not be null")
          .isNotNull()
          .returns(id, Category::getId)
          .returns("Groceries", Category::getName)
          .returns(50000L, Category::getMonthlyBudget);
    }
  }

  @Nested
  class ToModelList {

    @Test
    void should_MapEntitiesToModels() {
      // Given
      var id1 = UUID.randomUUID();
      var id2 = UUID.randomUUID();
      var entity1 = new CategoryEntity(id1, "Cat 1", UUID.randomUUID(), null);
      var entity2 = new CategoryEntity(id2, "Cat 2", UUID.randomUUID(), null);

      // When
      var models = categoryMapper.toModelList(List.of(entity1, entity2));

      // Then
      assertThat(models)
          .as("Mapped model list should have correct size and elements")
          .hasSize(2);

      assertThat(models.get(0))
          .as("First model should match first entity")
          .returns(id1, Category::getId)
          .returns("Cat 1", Category::getName);

      assertThat(models.get(1))
          .as("Second model should match second entity")
          .returns(id2, Category::getId)
          .returns("Cat 2", Category::getName);
    }
  }

  @Nested
  class UpdateEntity {

    @Test
    void should_OverwriteExistingEntity_But_PreserveMetadata() {
      // Given
      var originalId = UUID.randomUUID();
      var originalOwnerId = UUID.randomUUID();
      var originalCreatedAt = OffsetDateTime.now().minusDays(1);
      var originalUpdatedAt = OffsetDateTime.now().minusHours(1);
      var originalVersion = 10;

      var entity = new CategoryEntity(originalId, "Old Name", originalOwnerId, null);
      entity.setCreatedAt(originalCreatedAt);
      entity.setUpdatedAt(originalUpdatedAt);
      entity.setVersion(originalVersion);

      var update = new CategoryWrite()
          .name("New Name");

      // When
      categoryMapper.updateEntity(update, entity);

      // Then
      assertThat(entity)
          .as("Entity name should be updated, but metadata must be preserved")
          .returns("New Name", CategoryEntity::getName)
          .returns(originalId, CategoryEntity::getId)
          .returns(originalOwnerId, CategoryEntity::getOwnerId)
          .returns(originalVersion, CategoryEntity::getVersion)
          .returns(originalCreatedAt, CategoryEntity::getCreatedAt)
          .returns(originalUpdatedAt, CategoryEntity::getUpdatedAt);
    }
  }
}
