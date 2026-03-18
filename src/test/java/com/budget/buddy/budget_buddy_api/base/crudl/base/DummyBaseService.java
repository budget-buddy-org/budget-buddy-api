package com.budget.buddy.budget_buddy_api.base.crudl.base;

public class DummyBaseService extends AbstractBaseEntityService<DummyEntity, String, Object, Object, Object> {

  public DummyBaseService(
      BaseEntityRepository<DummyEntity, String> repository,
      BaseEntityMapper<DummyEntity, Object, Object, Object, Object> mapper
  ) {
    super(repository, mapper);
  }
}
