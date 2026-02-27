package com.budget.buddy.budget_buddy_api.repository;

import com.budget.buddy.budget_buddy_api.base.BaseRepository;
import com.budget.buddy.budget_buddy_api.entity.CategoryEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CategoryRepository extends BaseRepository<CategoryEntity, String> {

  /**
   * Find all categories by owner ID.
   *
   * @param ownerId the ID of the owner
   * @return list of categories owned by the specified owner
   */
  Optional<CategoryEntity> findByIdAndOwnerId(String id, String ownerId);

  /**
   * Find all categories by owner's username.
   *
   * @param username the username of the owner
   * @return list of categories owned by the specified username
   */
  List<CategoryEntity> findAllByOwnerUsername(String username);

  /**
   * Count the number of categories owned by a specific username.
   *
   * @param username the username of the owner
   * @return the count of categories owned by the specified username
   */
  long countByOwnerUsername(String username);

}
