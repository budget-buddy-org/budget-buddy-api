package com.budget.buddy.budget_buddy_api.base.crudl.base;

import lombok.RequiredArgsConstructor;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class BaseEntityListener<E extends BaseEntity<I>, I>
    implements BeforeConvertCallback<E> {

  private final Supplier<I> idGenerator;

  @Override
  public E onBeforeConvert(E entity) {
    if (entity.isNew()) {
      entity.setId(idGenerator.get());
    }

    return entity;
  }

}
