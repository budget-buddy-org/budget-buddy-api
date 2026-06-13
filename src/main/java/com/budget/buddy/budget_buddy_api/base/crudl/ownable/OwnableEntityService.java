package com.budget.buddy.budget_buddy_api.base.crudl.ownable;

import com.budget.buddy.budget_buddy_api.base.crudl.base.AbstractBaseEntityService;
import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityMapper;
import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityValidator;
import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for services managing entities that belong to a specific owner.
 *
 * <p>All CRUDL operations are automatically scoped to the owner returned by the
 * injected {@link OwnerIdProvider}. Subclasses inherit this scoping without any
 * additional wiring — the parent's {@code update/delete} flow is reused
 * verbatim because it dispatches through the overridden {@link #readInternal(Object)}.
 *
 * @param <E> the entity type
 * @param <ID> the identifier type
 * @param <R> the read model type (DTO)
 * @param <C> the create request type (DTO)
 * @param <U> the update request type (DTO) used for PUT updates
 */
public class OwnableEntityService<E extends OwnableEntity<ID>, ID, R, C, U, L>
    extends AbstractBaseEntityService<E, ID, R, C, U, L> {

  @Getter(AccessLevel.PROTECTED)
  private final OwnerIdProvider<ID> ownerIdProvider;

  protected OwnableEntityService(
      OwnableEntityRepository<E, ID> repository,
      BaseEntityMapper<E, R, C, U, L> mapper,
      Iterable<BaseEntityValidator<E>> entityValidators,
      OwnerIdProvider<ID> ownerIdProvider
  ) {
    super(repository, mapper, entityValidators);
    this.ownerIdProvider = ownerIdProvider;
  }

  @Override
  protected OwnableEntityRepository<E, ID> getRepository() {
    return (OwnableEntityRepository<E, ID>) super.getRepository();
  }

  @Override
  protected Page<E> listInternal(Pageable pageRequest) {
    return getRepository().findAllByOwnerId(ownerIdProvider.get(), pageRequest);
  }

  @Override
  protected E readInternal(ID id) {
    return getRepository().findByIdAndOwnerId(id, ownerIdProvider.get())
        .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE.formatted(id)));
  }

  @Override
  protected boolean existsByIdInternal(ID id) {
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
  @Transactional
  public void deleteAllByOwnerId() {
    getRepository().deleteAllByOwnerId(ownerIdProvider.get());
  }
}
