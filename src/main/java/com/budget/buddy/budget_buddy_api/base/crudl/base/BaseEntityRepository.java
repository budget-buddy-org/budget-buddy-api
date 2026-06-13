package com.budget.buddy.budget_buddy_api.base.crudl.base;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base repository interface providing common CRUD operations for entities extending {@link BaseEntity}.
 *
 * @param <E> the entity type
 * @param <ID> the identifier type
 */
@NoRepositoryBean
public interface BaseEntityRepository<E extends BaseEntity<ID>, ID>
    extends ListCrudRepository<E, ID>, ListPagingAndSortingRepository<E, ID> {

}
