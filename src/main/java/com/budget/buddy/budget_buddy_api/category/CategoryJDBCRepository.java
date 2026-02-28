package com.budget.buddy.budget_buddy_api.category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface CategoryJDBCRepository extends CrudRepository<CategoryEntity, UUID>, CategoryRepository {

  String FIND_ALL_BY_OWNER_USERNAME_QUERY = """
      SELECT c.*
      FROM categories c
      JOIN users u ON c.owner_id = u.id
      WHERE u.username = :username
      """;

  String COUNT_BY_OWNER_USERNAME_QUERY = """
      SELECT COUNT(1)
      FROM categories c
      JOIN users u ON c.owner_id = u.id
      WHERE u.username = :username
      """;

  @Override
  @Query(FIND_ALL_BY_OWNER_USERNAME_QUERY)
  List<CategoryEntity> findAllByOwnerUsername(String username);

  @Override
  @Query(COUNT_BY_OWNER_USERNAME_QUERY)
  long countByOwnerUsername(String username);

  @Override
  Optional<CategoryEntity> findByIdAndOwnerId(UUID id, UUID ownerId);

}
