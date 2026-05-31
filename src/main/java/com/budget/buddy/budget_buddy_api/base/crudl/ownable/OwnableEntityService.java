package com.budget.buddy.budget_buddy_api.base.crudl.ownable;

import com.budget.buddy.budget_buddy_api.base.crudl.base.AbstractBaseEntityService;
import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityMapper;
import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityValidator;
import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Base class for services managing entities that belong to a specific owner.
 *
 * <p>All CRUDL operations are automatically scoped to the owner returned by the
 * injected {@link OwnerIdProvider}. Subclasses inherit this scoping without any
 * additional wiring — the parent's {@code update/delete} flow is reused
 * verbatim because it dispatches through the overridden {@link #readInternal(Object)}.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 * @param <R> the read model type (DTO)
 * @param <C> the create request type (DTO)
 * @param <U> the update request type (DTO) used for PUT updates
 */
public class OwnableEntityService<E extends OwnableEntity<I>, I, R, C, U>
    extends AbstractBaseEntityService<E, I, R, C, U> {

  @Getter(AccessLevel.PROTECTED)
  private final OwnerIdProvider<I> ownerIdProvider;

  protected OwnableEntityService(
      OwnableEntityRepository<E, I> repository,
      BaseEntityMapper<E, R, C, U, ?> mapper,
      Iterable<BaseEntityValidator<E>> entityValidators,
      OwnerIdProvider<I> ownerIdProvider
  ) {
    super(repository, mapper, entityValidators);
    this.ownerIdProvider = ownerIdProvider;
  }

  @Override
  protected OwnableEntityRepository<E, I> getRepository() {
    return (OwnableEntityRepository<E, I>) super.getRepository();
  }

  @Override
  protected Page<E> listInternal(Pageable pageRequest) {
    return getRepository().findAllByOwnerId(ownerIdProvider.get(), pageRequest);
  }

  @Override
  protected E readInternal(I id) {
    return getRepository().findByIdAndOwnerId(id, ownerIdProvider.get())
        .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE.formatted(id)));
  }

  @Override
  protected boolean existsByIdInternal(I id) {
    return getRepository().existsByIdAndOwnerId(id, ownerIdProvider.get());
  }

  @Override
  protected E createInternal(C createRequest) {
    E entity = getMapper().toEntity(createRequest);
    entity.setOwnerId(ownerIdProvider.get());
    validate(entity);
    return getRepository().save(entity);
  }

  @Override
  protected long countInternal() {
    return getRepository().countByOwnerId(ownerIdProvider.get());
  }

  /**
   * Deletes every entity owned by the current owner. Used when clearing all of a user's data.
   */
  public void deleteAllByOwnerId() {
    getRepository().deleteAllByOwnerId(ownerIdProvider.get());
  }
}
