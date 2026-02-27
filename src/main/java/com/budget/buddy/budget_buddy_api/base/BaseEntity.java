package com.budget.buddy.budget_buddy_api.base;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity {

  @Id
  @Column("id")
  private String id;

  @Version
  @Column("version")
  private Integer version;

  @Column("created_at")
  private OffsetDateTime createdAt;

  @Column("updated_at")
  private OffsetDateTime updatedAt;

}
