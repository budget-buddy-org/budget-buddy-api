package com.budget.buddy.budget_buddy_api.base.crudl.base;

import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

/**
 * Base controller class providing common internal methods for CRUD operations. Designed to be extended by controllers that implement generated API interfaces.
 *
 * @param <ID> the identifier type
 * @param <C> the create request type (DTO)
 * @param <R> the read model type (DTO)
 * @param <U> the update request type (DTO) used for PUT updates
 * @param <L> the list response type (DTO)
 */
public abstract class BaseEntityController<ID, C, R, U, L> {

  private final BaseEntityService<ID, C, R, U, L> service;

  protected BaseEntityController(BaseEntityService<ID, C, R, U, L> service) {
    this.service = service;
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
   * Internal method to read an entity by ID.
   *
   * @param id the entity identifier
   * @return {@link ResponseEntity} with the entity
   */
  public ResponseEntity<R> readInternal(ID id) {
    var item = service.read(id);
    return ResponseEntity.ok(item);
  }

  /**
   * Internal method to fully update an entity by ID.
   *
   * @param id the entity identifier
   * @param updateRequest the update request (all fields required)
   * @return {@link ResponseEntity} with the updated entity
   */
  public ResponseEntity<R> updateInternal(ID id, U updateRequest) {
    var updated = service.update(id, updateRequest);
    return ResponseEntity.ok(updated);
  }

  /**
   * Internal method to delete an entity by ID.
   *
   * @param id the entity identifier
   * @return {@link ResponseEntity} with no content
   */
  public ResponseEntity<Void> deleteInternal(ID id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Internal method to list entities with a {@link Pageable}.
   *
   * @param pageable the pageable request
   * @return {@link ResponseEntity} with the paginated response
   */
  public ResponseEntity<L> listInternal(Pageable pageable) {
    return ResponseEntity.ok(service.list(pageable));
  }

  /**
   * Generates the URI for a newly created resource.
   *
   * @param created the created resource
   * @return the URI of the resource
   */
  protected abstract URI createdURI(R created);
}
