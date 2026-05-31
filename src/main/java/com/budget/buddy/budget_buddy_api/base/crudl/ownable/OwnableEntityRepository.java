package com.budget.buddy.budget_buddy_api.base.crudl.ownable;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface OwnableEntityRepository<E extends OwnableEntity<I>, I>
    extends BaseEntityRepository<E, I> {

  Page<E> findAllByOwnerId(I ownerId, Pageable pageable);

  Optional<E> findByIdAndOwnerId(I id, I ownerId);

  boolean existsByIdAndOwnerId(I id, I ownerId);

  long countByOwnerId(I ownerId);

  void deleteAllByOwnerId(I ownerId);
}
