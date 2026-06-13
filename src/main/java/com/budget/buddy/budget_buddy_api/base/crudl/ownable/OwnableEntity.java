package com.budget.buddy.budget_buddy_api.base.crudl.ownable;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;

/**
 * Abstract base class for entities that belong to a specific owner.
 *
 * <p>Extends {@link BaseEntity} with an {@code ownerId} field. All CRUDL operations on
 * subclasses are automatically scoped to the authenticated owner by
 * {@link OwnableEntityService} — no extra wiring required in subclasses.
 *
 * <p>Type parameter {@code ID} is used for both the entity id and the owner id.
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class OwnableEntity<ID> extends BaseEntity<ID> {

  @Column("owner_id")
  private ID ownerId;
}
