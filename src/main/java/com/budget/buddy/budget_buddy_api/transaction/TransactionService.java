package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityValidator;
import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntityService;
import com.budget.buddy.budget_buddy_api.generated.model.Transaction;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionCreate;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionUpdate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TransactionService extends
    OwnableEntityService<TransactionEntity, UUID, Transaction, TransactionCreate, TransactionUpdate> {

  private final TransactionRepository repository;
  private final TransactionMapper mapper;

  public TransactionService(
      TransactionRepository repository,
      TransactionMapper mapper,
      Set<BaseEntityValidator<TransactionEntity>> validators,
      Converter<String, UUID> ownerIdConverter
  ) {
    super(repository, mapper, validators, ownerIdConverter);
    this.repository = repository;
    this.mapper = mapper;
  }

  public List<Transaction> list(
      TransactionFilter filter,
      Pageable pageable
  ) {
    var entities = repository.findAllByFilter(filter.withOwnerId(getRequieredOnwerId()), pageable);
    return mapper.toModelList(entities);
  }


  public long count(TransactionFilter filter) {
    return repository.countByFilter(filter.withOwnerId(getRequieredOnwerId()));
  }

}
