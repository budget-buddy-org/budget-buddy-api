package com.budget.buddy.budget_buddy_api.base.crudl.ownable;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Base mapper interface for ownable entities.
 *
 * <p>Extends {@link BaseEntityMapper} with an additional {@code ownerId} ignore mapping so that
 * the owner-scoping field is never overwritten during create or update operations.
 *
 * <p>Type parameters match {@link BaseEntityMapper}; {@code E} is further constrained to
 * {@link OwnableEntity}.
 */
public interface OwnableEntityMapper<E extends OwnableEntity<?>, C, R, U, L>
    extends BaseEntityMapper<E, C, R, U, L> {

  @Override
  @BeanMapping(builder = @Builder(disableBuilder = true))
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "ownerId", ignore = true)
  E toEntity(C createRequest);

  @Override
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "ownerId", ignore = true)
  void updateEntity(U updateRequest, @MappingTarget E existingEntity);
}
