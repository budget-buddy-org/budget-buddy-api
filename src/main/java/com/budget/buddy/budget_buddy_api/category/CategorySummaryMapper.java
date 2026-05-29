package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.config.MapstructConfig;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategorySpendingRow;
import org.mapstruct.Mapper;

@Mapper(config = MapstructConfig.class)
public interface CategorySummaryMapper {

  CategorySpendingRow toModel(CategorySummaryRow row);

}
