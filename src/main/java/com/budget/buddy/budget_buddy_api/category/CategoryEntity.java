package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntity;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Category entity representing a budget category for organizing transactions. Uses Spring Data JDBC for data access.
 */
@Table("categories")
@Getter
@Setter
@NoArgsConstructor
public class CategoryEntity extends OwnableEntity<UUID> {

  @Column("name")
  private String name;

  @Column("monthly_budget")
  private @Nullable Long monthlyBudget;

}
