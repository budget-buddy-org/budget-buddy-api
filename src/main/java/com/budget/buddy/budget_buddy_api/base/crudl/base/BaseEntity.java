package com.budget.buddy.budget_buddy_api.base.crudl.base;

import java.time.OffsetDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;

/**
 * Abstract base class for all entities.
 *
 * <p>Provides identity and three audit fields, all managed automatically by the persistence
 * layer — never set them by hand:
 *
 * <ul>
 *   <li>{@code id} — assigned once on insert by {@code BaseEntityListener}.
 *   <li>{@code version} — optimistic-locking counter incremented on every update.
 *   <li>{@code createdAt} — timestamp (with offset) recorded once on insert.
 *   <li>{@code updatedAt} — timestamp (with offset) refreshed on every update.
 * </ul>
 *
 * <p>Spring Data JDBC populates these fields after construction via setters, so a
 * no-arg constructor legitimately leaves them unset.
 *
 * @param <ID> the identifier type
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class BaseEntity<ID> implements Persistable<ID> {

  @Id
  @Column("id")
  private @Nullable ID id;

  @Version
  @Column("version")
  private @Nullable Integer version;

  @CreatedDate
  @Column("created_at")
  private @Nullable OffsetDateTime createdAt;

  @LastModifiedDate
  @Column("updated_at")
  private @Nullable OffsetDateTime updatedAt;

  @Override
  public boolean isNew() {
    return Objects.isNull(getId());
  }
}
