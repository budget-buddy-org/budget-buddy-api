package com.budget.buddy.budget_buddy_api.category;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_contracts.generated.model.Category;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategoryWrite;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CategoryMapperTest {

  private static final Clock FIXED_CLOCK = Clock.fixed(
      Instant.parse("2026-06-13T10:00:00Z"),
      ZoneOffset.UTC
  );

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
      var entity = CategoryEntity.builder().name("Groceries").monthlyBudget(50000L).build();
      entity.setId(id);

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
      var entity1 = CategoryEntity.builder().name("Cat 1").build();
      entity1.setId(id1);
      var entity2 = CategoryEntity.builder().name("Cat 2").build();
      entity2.setId(id2);

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
      var originalCreatedAt = OffsetDateTime.now(FIXED_CLOCK).minusDays(1);
      var originalUpdatedAt = OffsetDateTime.now(FIXED_CLOCK).minusHours(1);
      var originalVersion = 10;

      var entity = CategoryEntity.builder().name("Old Name").build();
      entity.setId(originalId);
      entity.setOwnerId(originalOwnerId);
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
