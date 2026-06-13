package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.config.MapstructConfig;
import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntityMapper;
import com.budget.buddy.budget_buddy_api.base.mapper.CurrencyMapper;
import com.budget.buddy.budget_buddy_contracts.generated.model.PaginatedTransactions;
import com.budget.buddy.budget_buddy_contracts.generated.model.Transaction;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionWrite;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;

@Mapper(config = MapstructConfig.class, uses = CurrencyMapper.class)
public interface TransactionMapper
    extends OwnableEntityMapper<TransactionEntity, Transaction, TransactionWrite, TransactionWrite, PaginatedTransactions> {

  @Nullable
  TransactionType toModel(com.budget.buddy.budget_buddy_contracts.generated.model.@Nullable TransactionType source);
}
