package com.budget.buddy.budget_buddy_api.base.crudl.base;

import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import java.util.List;
import org.springframework.data.domain.Pageable;

/**
 * Generic CRUDL (Create, Read, Update, Delete, List) service interface for managing entities.
 *
 * @param <ID> The type of the unique identifier for the entities managed by the service.
 * @param <R> The type of the resource/model returned by the service methods.
 * @param <C> The type of the create request object used for creating new entities.
 * @param <U> The type of the update request object used for fully updating existing entities via PUT.
 * @param <L> The type of the paginated list response.
 */
public interface BaseEntityService<ID, R, C, U, L> {

  /**
   * Create a new entity based on the provided createRequest request object.
   *
   * @param createRequest The createRequest request object containing the data for the new entity.
   * @return The created resource/model representing the new entity.
   */
  R create(C createRequest);

  /**
   * Read an existing entity by its unique identifier.
   *
   * @param id The unique identifier of the entity to read.
   * @return The resource/model representing the entity with the specified I.
   * @throws EntityNotFoundException If no entity with the specified I exists.
   */
  R read(ID id) throws EntityNotFoundException;

  /**
   * Fully update an existing entity identified by its unique identifier. All writable fields are overwritten;
   * omitting optional fields clears them.
   *
   * @param id The unique identifier of the entity to update.
   * @param updateRequest The update request containing all fields for the update.
   * @return The updated resource/model.
   * @throws EntityNotFoundException If no entity with the specified I exists.
   */
  R update(ID id, U updateRequest) throws EntityNotFoundException;

  /**
   * Delete an existing entity by its unique identifier.
   *
   * @param id The unique identifier of the entity to delete.
   * @throws EntityNotFoundException If no entity with the specified I exists.
   */
  void delete(ID id) throws EntityNotFoundException;

  /**
   * List all existing entities.
   *
   * @return A list of resources/models representing all existing entities.
   */
  List<R> list();

  /**
   * List entities with pagination support.
   *
   * @param pageable The page request.
   * @return The paginated list response.
   */
  L list(Pageable pageable);

  /**
   * Count the total number of existing entities.
   *
   * @return The total count of existing entities.
   */
  long count();

  /**
   * Checks if an entity exists by its unique identifier.
   *
   * @param id the unique identifier
   * @return true if the entity exists, false otherwise
   */
  boolean existsById(ID id);
}
