package com.budget.buddy.budget_buddy_api.base.crudl.ownable;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface OwnableEntityRepository<E extends OwnableEntity<ID>, ID>
    extends BaseEntityRepository<E, ID> {

  Page<E> findAllByOwnerId(ID ownerId, Pageable pageable);

  Optional<E> findByIdAndOwnerId(ID id, ID ownerId);

  boolean existsByIdAndOwnerId(ID id, ID ownerId);

  long countByOwnerId(ID ownerId);
}
