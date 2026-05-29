package com.budget.buddy.budget_buddy_api.base.crudl.base;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class BaseEntityListener<E extends BaseEntity<I>, I>
    implements BeforeConvertCallback<E> {

  private final Supplier<I> idGenerator;

  @NonNull
  @Override
  public E onBeforeConvert(@NonNull E entity) {
    if (entity.isNew()) {
      entity.setId(idGenerator.get());
    }

    return entity;
  }

}
