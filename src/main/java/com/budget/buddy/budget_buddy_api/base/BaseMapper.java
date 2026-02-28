package com.budget.buddy.budget_buddy_api.base;

import java.util.List;

public interface BaseMapper<E extends BaseEntity<?>, R, C, U> {

  E toEntity(C createRequest);

  E toEntityForUpdate(U updateRequest);

  R toModel(E entity);

  List<R> toModelList(Iterable<E> entities);

}
