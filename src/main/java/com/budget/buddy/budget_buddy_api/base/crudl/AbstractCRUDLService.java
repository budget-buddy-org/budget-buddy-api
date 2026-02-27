package com.budget.buddy.budget_buddy_api.base.crudl;

import com.budget.buddy.budget_buddy_api.base.BaseEntity;
import com.budget.buddy.budget_buddy_api.base.BaseRepository;
import com.budget.buddy.budget_buddy_api.exception.EntityNotFoundException;
import com.budget.buddy.budget_buddy_api.mapper.BaseMapper;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Abstract base service class providing common CRUDL operations for entities. Subclasses can extend this class to implement specific business logic for different entity types.
 *
 * @param <E> Entity type extending BaseEntity
 * @param <R> Read model type (DTO)
 * @param <C> Create request type (DTO)
 * @param <U> Update request type (DTO)
 */
@RequiredArgsConstructor
public abstract class AbstractCRUDLService<E extends BaseEntity, R, C, U> implements CRUDLService<R, C, U> {

  private static final String ENTITY_NOT_FOUND_MESSAGE = "Entity not found with id: %s";

  @Getter
  private final BaseRepository<E, String> repository;
  @Getter
  private final BaseMapper<E, R, C, U> mapper;

  @Transactional
  @Override
  public R create(C createRequest) {
    E savedEntity = createInternal(createRequest);
    return mapper.toModel(savedEntity);
  }

  @Override
  public R read(String id) {
    E entity = readInternal(id);
    return mapper.toModel(entity);
  }

  @Transactional
  @Override
  public R update(String id, U updateRequest) {
    E updatedEntity = updateInternal(id, updateRequest);
    return mapper.toModel(updatedEntity);
  }

  @Transactional
  @Override
  public void delete(String id) {
    repository.deleteById(id);
  }

  @Override
  public List<R> list() {
    List<E> entities = listInternal();
    return mapper.toModelList(entities);
  }

  @Override
  public List<R> list(int limit, int offset) {
    List<E> entities = listInternal();
    int end = Math.min(offset + limit, entities.size());
    List<E> page = entities.subList(offset, end);
    return mapper.toModelList(page);
  }

  @Override
  public long count() {
    return repository.count();
  }

  protected E readInternal(String id) {
    return repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE.formatted(id)));
  }

  protected List<E> listInternal() {
    return repository.findAll();
  }

  protected E createInternal(C createRequest) {
    E entity = mapper.toEntity(createRequest);
    return repository.save(entity);
  }

  protected E updateInternal(String id, U updateRequest) {
    if (!repository.existsById(id)) {
      throw new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE.formatted(id));
    }

    E entity = mapper.toEntityForUpdate(updateRequest);
    entity.setId(id);

    return repository.save(entity);
  }

}
