package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnerIdProvider;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategorySpendingSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategorySummaryService {

  private static final DateTimeFormatter YEAR_MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

  private final CategorySummaryRepository repository;
  private final CategorySummaryMapper mapper;
  private final OwnerIdProvider<UUID> ownerIdProvider;

  public CategorySpendingSummary getSummary(String month, String currency) {
    var yearMonth = YearMonth.parse(month, YEAR_MONTH_FORMAT);
    var start = yearMonth.atDay(1);
    var end = yearMonth.atEndOfMonth();
    var ownerId = ownerIdProvider.get();

    var rows = repository.getSummary(ownerId, start, end, currency)
        .stream()
        .map(mapper::toModel)
        .toList();
    return new CategorySpendingSummary(month, currency, rows);
  }

}
