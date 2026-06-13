package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.config.MapstructConfig;
import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntityMapper;
import com.budget.buddy.budget_buddy_contracts.generated.model.Category;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategoryWrite;
import com.budget.buddy.budget_buddy_contracts.generated.model.PaginatedCategories;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(config = MapstructConfig.class)
public interface CategoryMapper
    extends OwnableEntityMapper<CategoryEntity, CategoryWrite, Category, CategoryWrite, PaginatedCategories> {

  @Override
  @Mapping(source = "content", target = "items")
  @Mapping(source = "entityPage", target = "meta")
  PaginatedCategories toPage(Page<CategoryEntity> entityPage);

}
