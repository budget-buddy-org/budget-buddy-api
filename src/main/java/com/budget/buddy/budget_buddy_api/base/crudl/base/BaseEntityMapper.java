package com.budget.buddy.budget_buddy_api.base.crudl.base;

import com.budget.buddy.budget_buddy_contracts.generated.model.PaginationMeta;
import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Base interface for entity mappers using MapStruct.
 *
 * @param <E> the entity type
 * @param <C> the create request type (DTO)
 * @param <R> the read model type (DTO)
 * @param <U> the update request type (DTO) used for PUT updates
 * @param <L> the list response type (DTO)
 */
public interface BaseEntityMapper<E extends BaseEntity<?>, C, R, U, L> {

  /**
   * Maps a create request to an entity.
   *
   * @param createRequest the create request
   * @return the mapped entity
   */
  E toEntity(C createRequest);

  /**
   * Maps an entity to a read model.
   *
   * @param entity the entity
   * @return the mapped read model
   */
  R toModel(E entity);

  /**
   * Maps a list of entities to a list of read models.
   *
   * @param entities the iterable of entities
   * @return the list of read models
   */
  List<R> toModelList(Iterable<E> entities);

  L toPage(Page<E> page);

  default PaginationMeta toPaginationMeta(Page<?> page) {
    var meta = new PaginationMeta();
    meta.setPage(page.getNumber());
    meta.setSize(page.getSize());
    meta.setTotal(page.getTotalElements());
    return meta;
  }

  /**
   * Fully updates an existing entity with values from an update request. All writable fields are overwritten,
   * including nulls. Immutable fields (id, version, createdAt, updatedAt) are preserved.
   *
   * @param updateRequest the update request
   * @param existingEntity the entity to update
   */
  void updateEntity(U updateRequest, E existingEntity);

}
