package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityController;
import com.budget.buddy.budget_buddy_contracts.generated.api.TransactionsApi;
import com.budget.buddy.budget_buddy_contracts.generated.model.MonthlySummary;
import com.budget.buddy.budget_buddy_contracts.generated.model.PaginatedTransactions;
import com.budget.buddy.budget_buddy_contracts.generated.model.Transaction;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionType;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionWrite;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Transaction controller for CRUDL operations on transactions.
 */
@RestController
public class TransactionController
    extends BaseEntityController<UUID, Transaction, TransactionWrite, TransactionWrite, PaginatedTransactions>
    implements TransactionsApi {

  private static final Sort DEFAULT_SORT = Sort.by(Direction.DESC, "date");

  private final TransactionService service;
  private final TransactionMapper mapper;
  private final TransactionSummaryService summaryService;

  public TransactionController(
      TransactionService service,
      TransactionMapper mapper,
      TransactionSummaryService summaryService) {
    super(service, mapper);
    this.service = service;
    this.mapper = mapper;
    this.summaryService = summaryService;
  }

  @Override
  public ResponseEntity<MonthlySummary> getTransactionsSummary(String month, String currency) {
    return ResponseEntity.ok(summaryService.getSummary(month, currency));
  }

  @Override
  public ResponseEntity<List<MonthlySummary>> getTransactionsSummaryTrend(String from, String to, String currency) {
    return ResponseEntity.ok(summaryService.getTrend(from, to, currency));
  }

  @Override
  public ResponseEntity<PaginatedTransactions> listTransactions(
      Integer page, Integer size,
      @Nullable String query, @Nullable Long amountMin, @Nullable Long amountMax,
      @Nullable UUID categoryId, @Nullable LocalDate start, @Nullable LocalDate end,
      @Nullable TransactionType type, String sort
  ) {
    var pageable = PageRequest.of(page, size, buildSort(sort));
    var filter = TransactionFilter.builder()
        .categoryId(categoryId)
        .start(start)
        .end(end)
        .type(mapper.toModel(type))
        .query(query)
        .amountMin(amountMin)
        .amountMax(amountMax)
        .build();
    return listTransactions(filter, pageable);
  }

  ResponseEntity<PaginatedTransactions> listTransactions(TransactionFilter filter, Pageable pageable) {
    var items = service.list(filter, pageable);
    return ResponseEntity.ok(mapper.toPageResponse(items.getContent(), toMeta(items)));
  }

  @Override
  public ResponseEntity<Transaction> createTransaction(TransactionWrite transactionCreate) {
    return super.createInternal(transactionCreate);
  }

  @Override
  public ResponseEntity<Void> deleteTransaction(UUID transactionId) {
    return super.deleteInternal(transactionId);
  }

  @Override
  public ResponseEntity<Transaction> getTransaction(UUID transactionId) {
    return super.readInternal(transactionId);
  }

  @Override
  public ResponseEntity<Transaction> updateTransaction(UUID transactionId, TransactionWrite transactionUpdate) {
    return super.updateInternal(transactionId, transactionUpdate);
  }

  @Override
  protected URI createdURI(Transaction created) {
    return URI.create("/v1/transactions/" + created.getId());
  }

  private static Sort buildSort(String sortStr) {
    return Direction.fromOptionalString(sortStr)
        .map(direction -> Sort.by(direction, "date"))
        .orElse(DEFAULT_SORT);
  }
}
