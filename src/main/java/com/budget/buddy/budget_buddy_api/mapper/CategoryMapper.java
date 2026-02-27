package com.budget.buddy.budget_buddy_api.mapper;

import com.budget.buddy.budget_buddy_api.entity.CategoryEntity;
import com.budget.buddy.budget_buddy_api.model.Category;
import com.budget.buddy.budget_buddy_api.model.CategoryCreate;
import com.budget.buddy.budget_buddy_api.model.CategoryUpdate;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper for Category entities to DTO models. Handles conversion between CategoryEntity and Category models.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper extends BaseMapper<CategoryEntity, Category, CategoryCreate, CategoryUpdate> {

  CategoryEntity toEntity(CategoryCreate request, String ownerId);

}
