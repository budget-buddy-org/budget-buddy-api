package com.budget.buddy.budget_buddy_api.service;

import com.budget.buddy.budget_buddy_api.base.crudl.AbstractCRUDLService;
import com.budget.buddy.budget_buddy_api.entity.CategoryEntity;
import com.budget.buddy.budget_buddy_api.exception.EntityNotFoundException;
import com.budget.buddy.budget_buddy_api.mapper.CategoryMapper;
import com.budget.buddy.budget_buddy_api.model.Category;
import com.budget.buddy.budget_buddy_api.model.CategoryCreate;
import com.budget.buddy.budget_buddy_api.model.CategoryUpdate;
import com.budget.buddy.budget_buddy_api.repository.CategoryRepository;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for category operations.
 */
@Service
public class CategoryService extends AbstractCRUDLService<CategoryEntity, Category, CategoryCreate, CategoryUpdate> {

  private final UserService userService;
  private final CategoryRepository categoryRepository;
  private final CategoryMapper categoryMapper;

  public CategoryService(UserService userService, CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
    super(categoryRepository, categoryMapper);
    this.userService = userService;
    this.categoryRepository = categoryRepository;
    this.categoryMapper = categoryMapper;
  }

  @Override
  public long count() {
    return userService.getCurrentUserName()
        .map(categoryRepository::countByOwnerUsername)
        .orElse(0L);
  }

  @Override
  @Transactional
  public void delete(String categoryId) {
    var entity = readInternal(categoryId);
    categoryRepository.deleteById(entity.getId());
  }

  @Override
  @Transactional
  protected CategoryEntity createInternal(CategoryCreate createRequest) {
    var ownerId = userService.getCurrentUserIdOrThrow();
    var entity = categoryMapper.toEntity(createRequest, ownerId);

    return categoryRepository.save(entity);
  }

  @Override
  protected CategoryEntity readInternal(String categoryId) {
    var ownerId = userService.getCurrentUserIdOrThrow();
    return categoryRepository.findByIdAndOwnerId(categoryId, ownerId)
        .orElseThrow(() -> new EntityNotFoundException("Category not found"));
  }

  @Override
  @Transactional
  protected CategoryEntity updateInternal(String categoryId, CategoryUpdate request) {
    var entity = readInternal(categoryId);
    entity.setName(request.getName());

    return categoryRepository.save(entity);
  }

  @Override
  protected List<CategoryEntity> listInternal() {
    return userService.getCurrentUserName()
        .map(categoryRepository::findAllByOwnerUsername)
        .orElseGet(Collections::emptyList);
  }
}
