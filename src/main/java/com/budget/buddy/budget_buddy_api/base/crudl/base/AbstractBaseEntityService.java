package com.budget.buddy.budget_buddy_api.base.crudl.base;

import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Abstract base service class providing common CRUDL operations for entities. Subclasses can extend this class to implement specific business logic for different entity types.
 *
 * @param <E> entity type extending BaseEntity
 * @param <ID> identifier type
 * @param <C> create request type (DTO)
 * @param <R> read model type (DTO)
 * @param <U> update request type (DTO) used for PUT updates
 * @param <L> paginated list response type (DTO)
 */
@Slf4j
@Transactional(readOnly = true)
public abstract class AbstractBaseEntityService<E extends BaseEntity<ID>, ID, C, R, U, L>
    implements BaseEntityService<ID, C, R, U, L> {

  protected static final String ENTITY_NOT_FOUND_MESSAGE = "Entity not found with id: %s";

  @Getter(AccessLevel.PROTECTED)
  private final BaseEntityRepository<E, ID> repository;

  @Getter(AccessLevel.PROTECTED)
  private final BaseEntityMapper<E, C, R, U, L> mapper;

  private final Iterable<BaseEntityValidator<E>> validators;

  protected AbstractBaseEntityService(
      BaseEntityRepository<E, ID> repository,
      BaseEntityMapper<E, C, R, U, L> mapper
  ) {
    this(repository, mapper, Collections.emptyList());
  }

  protected AbstractBaseEntityService(
      BaseEntityRepository<E, ID> repository,
      BaseEntityMapper<E, C, R, U, L> mapper,
      Iterable<BaseEntityValidator<E>> validators
  ) {
    this.repository = repository;
    this.mapper = mapper;
    this.validators = validators;
  }

  @Transactional
  @Override
  public R create(C createRequest) {
    E savedEntity = createInternal(createRequest);
    log.info("Created entity: id={}", savedEntity.getId());
    return mapper.toModel(savedEntity);
  }

  @Override
  public R read(ID id) {
    log.debug("Read entity: id={}", id);
    return mapper.toModel(readInternal(id));
  }

  @Transactional
  @Override
  public R update(ID id, U updateRequest) {
    log.debug("Update entity: id={}", id);
    return mapper.toModel(updateInternal(id, updateRequest));
  }

  @Transactional
  @Override
  public void delete(ID id) {
    deleteInternal(id);
    log.info("Deleted entity: id={}", id);
  }

  @Override
  public List<R> list() {
    log.debug("List entities");
    return mapper.toModelList(listInternal());
  }

  @Override
  public L list(Pageable pageable) {
    log.debug("List entities: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
    return mapper.toPage(listInternal(pageable));
  }

  @Override
  public long count() {
    log.debug("Count entities");
    return countInternal();
  }

  @Override
  public boolean existsById(ID id) {
    return existsByIdInternal(id);
  }

  /**
   * Checks if an entity exists by its unique identifier.
   *
   * @param id the unique identifier
   * @return true if the entity exists, false otherwise
   */
  protected boolean existsByIdInternal(ID id) {
    return repository.existsById(id);
  }

  /**
   * Logic to create a new entity.
   *
   * @param createRequest the create request
   * @return the created entity
   */
  protected E createInternal(C createRequest) {
    E entity = mapper.toEntity(createRequest);
    validate(entity);
    return repository.save(entity);
  }

  /**
   * Logic to read an entity by its unique identifier.
   *
   * @param id the unique identifier
   * @return the entity
   * @throws EntityNotFoundException if the entity is not found
   */
  protected E readInternal(ID id) {
    return repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE.formatted(id)));
  }

  /**
   * Logic to fully update an entity.
   *
   * @param id the unique identifier
   * @param updateRequest the update request
   * @return the updated entity
   */
  protected E updateInternal(ID id, U updateRequest) {
    E existingEntity = readInternal(id);
    mapper.updateEntity(updateRequest, existingEntity);
    validate(existingEntity);
    return repository.save(existingEntity);
  }

  /**
   * Logic to delete an entity.
   *
   * @param id the unique identifier
   */
  protected void deleteInternal(ID id) {
    var entity = readInternal(id);
    repository.delete(entity);
  }

  /**
   * Logic to list all entities.
   *
   * @return list of entities
   */
  protected List<E> listInternal() {
    return repository.findAll();
  }

  /**
   * Logic to list entities with pagination.
   *
   * @param pageRequest the page request
   * @return page of entities
   */
  protected Page<E> listInternal(Pageable pageRequest) {
    return repository.findAll(pageRequest);
  }

  /**
   * Logic to count all entities.
   *
   * @return total count
   */
  protected long countInternal() {
    return repository.count();
  }

  /**
   * Validates an entity using registered validators.
   *
   * @param entity the entity to validate
   */
  protected void validate(E entity) {
    validators.forEach(v -> v.validate(entity));
  }

}
