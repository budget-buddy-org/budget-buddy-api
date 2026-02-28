package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.BaseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CategoryRepository extends BaseRepository<CategoryEntity, UUID> {

  /**
   * Find all categories by owner ID.
   *
   * @param ownerId the ID of the owner
   * @return list of categories owned by the specified owner
   */
  Optional<CategoryEntity> findByIdAndOwnerId(UUID id, UUID ownerId);

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
