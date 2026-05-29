package com.budget.buddy.budget_buddy_api.base.crudl.base;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * BaseRepository is a generic interface that defines common CRUD operations for entities that extend BaseEntity.
 *
 * @param <E> the type of the entity
 * @param <I> the type of the entity's identifier
 */
@NoRepositoryBean
public interface BaseEntityRepository<E extends BaseEntity<I>, I>
    extends ListCrudRepository<E, I>, ListPagingAndSortingRepository<E, I> {

}
