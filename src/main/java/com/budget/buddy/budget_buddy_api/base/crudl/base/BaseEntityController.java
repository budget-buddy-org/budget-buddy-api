package com.budget.buddy.budget_buddy_api.base.crudl.base;

import com.budget.buddy.budget_buddy_contracts.generated.model.PaginationMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.net.URI;

/**
 * Base controller class providing common internal methods for CRUD operations. Designed to be extended by controllers that implement generated API interfaces.
 *
 * @param <I> the identifier type
 * @param <R> the read model type (DTO)
 * @param <C> the create request type (DTO)
 * @param <U> the update request type (DTO) used for PUT updates
 * @param <L> the list response type (DTO)
 */
public abstract class BaseEntityController<I, R, C, U, L> {

  private final BaseEntityService<I, R, C, U> service;
  private final BaseEntityMapper<?, R, C, U, L> mapper;

  protected BaseEntityController(
      BaseEntityService<I, R, C, U> service,
      BaseEntityMapper<?, R, C, U, L> mapper
  ) {
    this.service = service;
    this.mapper = mapper;
  }

  /**
   * Internal method to create an entity.
   *
   * @param createRequest the create request
   * @return {@link ResponseEntity} with the created entity and location header
   */
  public ResponseEntity<R> createInternal(C createRequest) {
    var created = service.create(createRequest);
    return ResponseEntity
        .created(createdURI(created))
        .body(created);
  }

  /**
   * Internal method to read an entity by I.
   *
   * @param id the entity identifier
   * @return {@link ResponseEntity} with the entity
   */
  public ResponseEntity<R> readInternal(I id) {
    var item = service.read(id);
    return ResponseEntity.ok(item);
  }

  /**
   * Internal method to fully update an entity by I.
   *
   * @param id the entity identifier
   * @param updateRequest the update request (all fields required)
   * @return {@link ResponseEntity} with the updated entity
   */
  public ResponseEntity<R> updateInternal(I id, U updateRequest) {
    var updated = service.update(id, updateRequest);
    return ResponseEntity.ok(updated);
  }

  /**
   * Internal method to delete an entity by I.
   *
   * @param id the entity identifier
   * @return {@link ResponseEntity} with no content
   */
  public ResponseEntity<Void> deleteInternal(I id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Internal method to list entities with page and size.
   *
   * @param page zero-based page number
   * @param size the number of items per page
   * @return {@link ResponseEntity} with the paginated response
   */
  public ResponseEntity<L> listInternal(Integer page, Integer size) {
    return listInternal(PageRequest.of(page, size));
  }

  /**
   * Internal method to list entities with a {@link Pageable}.
   *
   * @param pageable the pageable request
   * @return {@link ResponseEntity} with the paginated response
   */
  public ResponseEntity<L> listInternal(Pageable pageable) {
    var items = service.list(pageable);
    var response = mapper.toPageResponse(items.getContent(), toMeta(items));
    return ResponseEntity.ok(response);
  }

  /**
   * Builds the {@link PaginationMeta} envelope from a {@link Page}, mirroring its page number,
   * size, and total element count.
   *
   * @param page the page to describe
   * @return the populated pagination metadata
   */
  protected static PaginationMeta toMeta(Page<?> page) {
    var meta = new PaginationMeta();
    meta.setPage(page.getNumber());
    meta.setSize(page.getSize());
    meta.setTotal(page.getTotalElements());
    return meta;
  }

  /**
   * Generates the URI for a newly created resource.
   *
   * @param created the created resource
   * @return the URI of the resource
   */
  protected abstract URI createdURI(R created);
}
