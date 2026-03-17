package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.AbstractBaseService;
import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import com.budget.buddy.budget_buddy_api.generated.model.Transaction;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionCreate;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionUpdate;
import com.budget.buddy.budget_buddy_api.security.auth.AuthUtils;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService extends
    AbstractBaseService<TransactionEntity, UUID, Transaction, TransactionCreate, TransactionUpdate> {

  private final TransactionRepository repository;
  private final TransactionMapper mapper;

  public TransactionService(TransactionRepository repository, TransactionMapper mapper) {
    super(repository, mapper);
    this.repository = repository;
    this.mapper = mapper;
  }

  public List<Transaction> list(UUID categoryId, LocalDate start, LocalDate end, Pageable pageable) {
    var ownerId = AuthUtils.requireCurrentUserId();
    var entities = repository.findAllByFilters(ownerId, start, end, categoryId, pageable);
    return mapper.toModelList(entities);
  }

  public long count(UUID categoryId, LocalDate start, LocalDate end) {
    var ownerId = AuthUtils.requireCurrentUserId();
    return repository.countByFilters(ownerId, start, end, categoryId);
  }

  @Override
  @Transactional
  protected TransactionEntity createInternal(TransactionCreate createRequest) {
    var ownerId = AuthUtils.requireCurrentUserId();
    var entity = mapper.toEntity(createRequest, ownerId);
    return repository.save(entity);
  }

  @Override
  protected TransactionEntity readInternal(UUID transactionId) {
    var ownerId = AuthUtils.requireCurrentUserId();
    return repository.findByIdAndOwnerId(transactionId, ownerId)
        .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
  }

  @Override
  protected Page<TransactionEntity> listInternal(Pageable pageable) {
    var ownerId = AuthUtils.requireCurrentUserId();
    return repository.findAllByOwnerId(ownerId, pageable);
  }

}
