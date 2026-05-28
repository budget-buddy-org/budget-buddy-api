package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.config.MapstructConfig;
import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityMapper;
import com.budget.buddy.budget_buddy_api.base.mapper.CurrencyMapper;
import com.budget.buddy.budget_buddy_contracts.generated.model.PaginatedTransactions;
import com.budget.buddy.budget_buddy_contracts.generated.model.Transaction;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionWrite;
import org.mapstruct.Mapper;

@Mapper(config = MapstructConfig.class, uses = CurrencyMapper.class)
public interface TransactionMapper
    extends BaseEntityMapper<TransactionEntity, Transaction, TransactionWrite, TransactionWrite, PaginatedTransactions> {

  TransactionType toModel(com.budget.buddy.budget_buddy_contracts.generated.model.TransactionType source);
}
