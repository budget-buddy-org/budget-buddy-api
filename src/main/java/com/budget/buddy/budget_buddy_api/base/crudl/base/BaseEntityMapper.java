package com.budget.buddy.budget_buddy_api.base.crudl.base;

import com.budget.buddy.budget_buddy_contracts.generated.model.PaginationMeta;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Base interface for entity mappers using MapStruct.
 *
 * @param <E> the entity type
 * @param <R> the read model type (DTO)
 * @param <C> the create request type (DTO)
 * @param <U> the update request type (DTO) used for PUT updates
 * @param <L> the list response type (DTO)
 */
public interface BaseEntityMapper<E extends BaseEntity<?>, R, C, U, L> {

  /**
   * Maps a create request to an entity.
   *
   * @param createRequest the create request
   * @return the mapped entity
   */
  @BeanMapping(builder = @Builder(disableBuilder = true))
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
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

  /**
   * Maps a list of items and pagination metadata to a page response.
   *
   * @param items the list of items
   * @param meta the pagination metadata
   * @return the page response
   */
  L toPageResponse(List<R> items, PaginationMeta meta);

  /**
   * Fully updates an existing entity with values from an update request. All writable fields are overwritten,
   * including nulls. Immutable fields (id, version, createdAt, updatedAt) are preserved.
   *
   * @param updateRequest the update request
   * @param existingEntity the entity to update
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntity(U updateRequest, @MappingTarget E existingEntity);

}
