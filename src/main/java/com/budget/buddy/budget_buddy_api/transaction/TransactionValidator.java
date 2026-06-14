package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityValidator;
import com.budget.buddy.budget_buddy_api.base.exception.ValidationException;
import com.budget.buddy.budget_buddy_api.category.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionValidator implements BaseEntityValidator<TransactionEntity> {

  private final CategoryService categoryService;

  @Override
  public void validate(TransactionEntity entity) {
    validateCategory(entity.getCategoryId());
  }

  private void validateCategory(@Nullable UUID categoryId) {
    if (categoryId == null) {
      throw new ValidationException("Category ID must be set");
    }

    log.debug("Validating transaction category: id={}", categoryId);
    if (!categoryService.existsById(categoryId)) {
      throw new ValidationException("Unknown category with id: " + categoryId);
    }
  }
}
