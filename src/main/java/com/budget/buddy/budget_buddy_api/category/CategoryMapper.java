package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.config.MapstructConfig;
import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityMapper;
import com.budget.buddy.budget_buddy_contracts.generated.model.Category;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategoryWrite;
import com.budget.buddy.budget_buddy_contracts.generated.model.PaginatedCategories;
import org.mapstruct.Mapper;

@Mapper(config = MapstructConfig.class)
public interface CategoryMapper
    extends BaseEntityMapper<CategoryEntity, Category, CategoryWrite, CategoryWrite, PaginatedCategories> {

}
