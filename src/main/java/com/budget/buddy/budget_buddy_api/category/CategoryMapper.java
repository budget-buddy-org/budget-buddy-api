package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntityMapper;
import com.budget.buddy.budget_buddy_api.generated.model.Category;
import com.budget.buddy.budget_buddy_api.generated.model.CategoryCreate;
import com.budget.buddy.budget_buddy_api.generated.model.CategoryUpdate;
import com.budget.buddy.budget_buddy_api.generated.model.PaginatedCategories;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper for Category entities to DTO models. Handles conversion between CategoryEntity and Category models.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper
    extends OwnableEntityMapper<CategoryEntity, UUID, Category, CategoryCreate, CategoryUpdate, PaginatedCategories> {

}
