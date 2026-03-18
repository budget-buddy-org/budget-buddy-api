package com.budget.buddy.budget_buddy_api.base.crudl.base;

@FunctionalInterface
public interface BaseEntityValidator<E extends BaseEntity<?>> {

  void validate(E entity);

}
