package com.budget.buddy.budget_buddy_api.repository;

import com.budget.buddy.budget_buddy_api.entity.CategoryEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface CategoryJDBCRepository extends CrudRepository<CategoryEntity, String>, CategoryRepository {

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

  @Query(FIND_ALL_BY_OWNER_USERNAME_QUERY)
  List<CategoryEntity> findAllByOwnerUsername(String username);

  @Query(COUNT_BY_OWNER_USERNAME_QUERY)
  long countByOwnerUsername(String username);

  Optional<CategoryEntity> findByIdAndOwnerId(String id, String ownerId);

}
