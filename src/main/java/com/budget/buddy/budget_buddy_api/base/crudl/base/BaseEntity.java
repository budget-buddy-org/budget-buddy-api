package com.budget.buddy.budget_buddy_api.base.crudl.base;

import org.springframework.data.domain.Persistable;

/**
 * Base interface for all entities in the application. Extends {@link Persistable} to provide standard persistence status.
 *
 * @param <I> the identifier type
 */
public interface BaseEntity<I> extends Persistable<I> {

  /**
   * Sets the unique identifier for the entity.
   *
   * @param id the unique identifier
   */
  void setId(I id);

  @Override
  default boolean isNew() {
    return getId() == null;
  }
}
