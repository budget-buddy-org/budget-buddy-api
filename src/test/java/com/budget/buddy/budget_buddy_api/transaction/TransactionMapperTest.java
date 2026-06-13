package com.budget.buddy.budget_buddy_api.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.base.mapper.CurrencyMapperImpl;
import com.budget.buddy.budget_buddy_contracts.generated.model.Transaction;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionWrite;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TransactionMapperTest {

  private final TransactionMapper transactionMapper = new TransactionMapperImpl(
      new CurrencyMapperImpl()
  );

  @Nested
  class ToEntity {

    @Test
    void should_MapTransactionWriteToTransactionEntity() {
      // Given
      var categoryId = UUID.randomUUID();
      var date = LocalDate.now();
      var create = new TransactionWrite(
          categoryId,
          1000L,
          com.budget.buddy.budget_buddy_contracts.generated.model.TransactionType.EXPENSE,
          "EUR",
          date
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
          .returns(Currency.getInstance("EUR"), TransactionEntity::getCurrency)
          .returns(date, TransactionEntity::getDate)
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
          .date(LocalDate.now());

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
      var date = LocalDate.now();

      var entity = TransactionEntity.builder()
          .categoryId(categoryId)
          .amount(500L)
          .type(TransactionType.INCOME)
          .currency(Currency.getInstance("USD"))
          .date(date)
          .description("Income description")
          .build();
      entity.setId(id);
      entity.setOwnerId(ownerId);

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
          .returns(date, Transaction::getDate)
          .returns("Income description", Transaction::getDescription);
    }
  }

  @Nested
  class ToModelList {

    @Test
    void should_MapEntitiesToModels() {
      // Given
      var id1 = UUID.randomUUID();
      var id2 = UUID.randomUUID();
      var e1 = TransactionEntity.builder().categoryId(UUID.randomUUID()).amount(100L).type(TransactionType.EXPENSE).currency(
          Currency.getInstance("EUR")).date(LocalDate.now()).description("D1").build();
      e1.setId(id1);
      var e2 = TransactionEntity.builder().categoryId(UUID.randomUUID()).amount(200L).type(TransactionType.INCOME).currency(
          Currency.getInstance("USD")).date(LocalDate.now()).description("D2").build();
      e2.setId(id2);

      // When
      var models = transactionMapper.toModelList(List.of(e1, e2));

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
      var originalCreatedAt = OffsetDateTime.now().minusDays(1);
      var originalUpdatedAt = OffsetDateTime.now().minusHours(1);
      var originalVersion = 5;

      var entity = TransactionEntity.builder()
          .categoryId(UUID.randomUUID())
          .amount(100L)
          .type(TransactionType.EXPENSE)
          .currency(Currency.getInstance("EUR"))
          .date(LocalDate.now())
          .description("Old Desc")
          .build();
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
          .date(LocalDate.now());

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
