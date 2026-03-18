package com.budget.buddy.budget_buddy_api.base.crudl.ownable;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntity;

@SuppressWarnings("java:S119")
public interface OwnableEntity<ID> extends BaseEntity<ID> {

  ID getOwnerId();

  void setOwnerId(ID id);

}
