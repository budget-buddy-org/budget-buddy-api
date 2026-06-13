package com.budget.buddy.budget_buddy_api.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.base.mapper.CurrencyMapperImpl;
import com.budget.buddy.budget_buddy_contracts.generated.model.Transaction;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionWrite;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TransactionMapperTest {

  private static final LocalDate FIXED_DATE = LocalDate.of(2024, Month.JUNE, 15);
  private static final OffsetDateTime FIXED_CREATED_AT = OffsetDateTime.of(
      LocalDateTime.of(2024, Month.JUNE, 14, 10, 0, 0, 0),
      ZoneOffset.UTC
  );
  private static final OffsetDateTime FIXED_UPDATED_AT = OffsetDateTime.of(
      LocalDateTime.of(2024, Month.JUNE, 15, 9, 0, 0, 0),
      ZoneOffset.UTC
  );
  private static final Currency EUR = Currency.getInstance("EUR");
  private static final Currency USD = Currency.getInstance("USD");

  private final TransactionMapper transactionMapper = new TransactionMapperImpl(
      new CurrencyMapperImpl()
  );

  @Nested
  class ToEntity {

    @Test
    void should_MapTransactionWriteToTransactionEntity() {
      // Given
      var categoryId = UUID.randomUUID();
      var create = new TransactionWrite(
          categoryId,
          1000L,
          com.budget.buddy.budget_buddy_contracts.generated.model.TransactionType.EXPENSE,
          "EUR",
          FIXED_DATE
      ).description("Test transaction");

      // When
      var entity = transactionMapper.toEntity(create);

      // Then
      assertThat(entity)
          .as("Mapped transaction entity should have correct values")
          .isNotNull()
          .returns(categoryId, TransactionEntity::getCategoryId)
          .returns(1000L, TransactionEntity::getAmount)
          .returns(TransactionType.EXPENSE, TransactionEntity::getType)
          .returns(EUR, TransactionEntity::getCurrency)
          .returns(FIXED_DATE, TransactionEntity::getDate)
          .returns("Test transaction", TransactionEntity::getDescription);
    }

    @Test
    void should_MapNullDescription_When_Cleared() {
      // Given
      var create = new TransactionWrite()
          .categoryId(UUID.randomUUID())
          .amount(1000L)
          .type(com.budget.buddy.budget_buddy_contracts.generated.model.TransactionType.EXPENSE)
          .currency("EUR")
          .date(FIXED_DATE);

      // When
      var entity = transactionMapper.toEntity(create);

      // Then
      assertThat(entity.getDescription())
          .as("Null description in the write model should map to null on the entity")
          .isNull();
    }
  }

  @Nested
  class ToModel {

    @Test
    void should_MapTransactionEntityToTransaction() {
      // Given
      var id = UUID.randomUUID();
      var categoryId = UUID.randomUUID();
      var ownerId = UUID.randomUUID();

      var entity = new TransactionEntity();
      entity.setId(id);
      entity.setOwnerId(ownerId);
      entity.setCategoryId(categoryId);
      entity.setAmount(500L);
      entity.setType(TransactionType.INCOME);
      entity.setCurrency(USD);
      entity.setDate(FIXED_DATE);
      entity.setDescription("Income description");

      // When
      var model = transactionMapper.toModel(entity);

      // Then
      assertThat(model)
          .as("Mapped transaction model should have correct values")
          .isNotNull()
          .returns(id, Transaction::getId)
          .returns(categoryId, Transaction::getCategoryId)
          .returns(500L, Transaction::getAmount)
          .returns("INCOME", m -> m.getType().getValue())
          .returns("USD", Transaction::getCurrency)
          .returns(FIXED_DATE, Transaction::getDate)
          .returns("Income description", Transaction::getDescription);
    }
  }

  @Nested
  class ToModelList {

    @Test
    void should_MapEntitiesToModels() {
      // Given
      var id1 = UUID.randomUUID();
      var entity1 = new TransactionEntity();
      entity1.setId(id1);
      entity1.setCategoryId(UUID.randomUUID());
      entity1.setAmount(100L);
      entity1.setType(TransactionType.EXPENSE);
      entity1.setCurrency(Currency.getInstance("EUR"));
      entity1.setDate(FIXED_DATE);
      entity1.setDescription("D1");

      var id2 = UUID.randomUUID();
      var entity2 = new TransactionEntity();
      entity2.setId(id2);
      entity2.setCategoryId(UUID.randomUUID());
      entity2.setAmount(200L);
      entity2.setType(TransactionType.INCOME);
      entity2.setCurrency(USD);
      entity2.setDate(FIXED_DATE);
      entity2.setDescription("D2");

      // When
      var models = transactionMapper.toModelList(List.of(entity1, entity2));

      // Then
      assertThat(models)
          .as("Mapped model list should have correct size and elements")
          .hasSize(2);

      assertThat(models.get(0))
          .as("First model should match first entity")
          .returns(id1, Transaction::getId)
          .returns(100L, Transaction::getAmount);

      assertThat(models.get(1))
          .as("Second model should match second entity")
          .returns(id2, Transaction::getId)
          .returns(200L, Transaction::getAmount);
    }
  }

  @Nested
  class UpdateEntity {

    @Test
    void should_OverwriteWithNull_When_ProvidedInUpdateRequest_But_PreserveMetadata() {
      // Given
      var originalId = UUID.randomUUID();
      var originalOwnerId = UUID.randomUUID();
      var originalCreatedAt = FIXED_CREATED_AT.minusDays(1);
      var originalUpdatedAt = FIXED_UPDATED_AT.minusHours(1);
      var originalVersion = 5;

      var entity = new TransactionEntity();
      entity.setCategoryId(UUID.randomUUID());
      entity.setAmount(100L);
      entity.setType(TransactionType.EXPENSE);
      entity.setCurrency(Currency.getInstance("EUR"));
      entity.setDate(FIXED_DATE);
      entity.setDescription("Old Desc");
      entity.setId(originalId);
      entity.setOwnerId(originalOwnerId);
      entity.setCreatedAt(originalCreatedAt);
      entity.setUpdatedAt(originalUpdatedAt);
      entity.setVersion(originalVersion);

      var update = new TransactionWrite()
          .categoryId(UUID.randomUUID())
          .amount(200L)
          .type(com.budget.buddy.budget_buddy_contracts.generated.model.TransactionType.INCOME)
          .currency("USD")
          .date(FIXED_DATE);

      // When
      transactionMapper.updateEntity(update, entity);

      // Then
      assertThat(entity)
          .as("CategoryId and amount should be updated")
          .returns(update.getCategoryId(), TransactionEntity::getCategoryId)
          .returns(200L, TransactionEntity::getAmount)
          .extracting(TransactionEntity::getDescription)
          .as("Description should be overwritten with null in update (PUT) operation")
          .isNull();

      // Verify metadata is preserved
      assertThat(entity)
          .as("Metadata and identity fields should not be changed by updateEntity")
          .returns(originalId, TransactionEntity::getId)
          .returns(originalOwnerId, TransactionEntity::getOwnerId)
          .returns(originalVersion, TransactionEntity::getVersion)
          .returns(originalCreatedAt, TransactionEntity::getCreatedAt)
          .returns(originalUpdatedAt, TransactionEntity::getUpdatedAt);
    }
  }
}
