package com.budget.buddy.budget_buddy_api.base.crudl.base;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BaseEntityListener<E extends BaseEntity<ID>, ID> implements BeforeConvertCallback<E> {

  private final Supplier<ID> idGenerator;

  @Override
  public E onBeforeConvert(E entity) {
    if (entity.isNew()) {
      entity.setId(idGenerator.get());
    }

    return entity;
  }

}
