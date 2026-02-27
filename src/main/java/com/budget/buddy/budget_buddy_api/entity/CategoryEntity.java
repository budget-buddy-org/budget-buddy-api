package com.budget.buddy.budget_buddy_api.entity;

import com.budget.buddy.budget_buddy_api.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Category entity representing a budget category for organizing transactions. Uses Spring Data JDBC for data access.
 */
@Table("categories")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity extends BaseEntity {

  @Column("name")
  private String name;

  @Column("owner_id")
  private String ownerId;

}
