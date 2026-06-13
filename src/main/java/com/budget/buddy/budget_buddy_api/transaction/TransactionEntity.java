package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntity;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Transaction entity representing a financial transaction. Uses Spring Data JDBC for data access.
 */
@Table("transactions")
@Getter
@Setter
@NoArgsConstructor
public class TransactionEntity extends OwnableEntity<UUID> {

  @Column("category_id")
  private UUID categoryId;

  @Column("amount")
  private Long amount;

  @Column("type")
  private TransactionType type;

  @Column("currency")
  private Currency currency;

  @Column("date")
  private LocalDate date;

  @Column("description")
  private String description;

}
