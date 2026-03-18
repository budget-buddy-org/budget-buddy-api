package com.budget.buddy.budget_buddy_api.base.crudl.ownable;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityMapper;

@SuppressWarnings("java:S119")
public interface OwnableEntityMapper<E extends OwnableEntity<ID>, ID, R, C, U, L>
    extends BaseEntityMapper<E, R, C, U, L> {

  E toEntity(C create, ID ownerId);

}
