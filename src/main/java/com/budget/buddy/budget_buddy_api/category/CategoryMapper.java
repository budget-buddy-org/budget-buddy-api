package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.BaseMapper;
import com.budget.buddy.budget_buddy_api.model.Category;
import com.budget.buddy.budget_buddy_api.model.CategoryCreate;
import com.budget.buddy.budget_buddy_api.model.CategoryUpdate;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper for Category entities to DTO models. Handles conversion between CategoryEntity and Category models.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper extends BaseMapper<CategoryEntity, Category, CategoryCreate, CategoryUpdate> {

  CategoryEntity toEntity(CategoryCreate request, UUID ownerId);

}
