package com.budget.buddy.budget_buddy_api.base.crudl.ownable;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntity;

/**
 * Interface for entities that belong to a user.
 *
 * @param <I> the user identifier type
 */
public interface OwnableEntity<I> extends BaseEntity<I> {

  /**
   * Retrieves the I of the user who owns this entity.
   *
   * @return the owner I
   */
  I getOwnerId();

  /**
   * Sets the I of the user who owns this entity.
   *
   * @param id the owner I
   */
  void setOwnerId(I id);

}
