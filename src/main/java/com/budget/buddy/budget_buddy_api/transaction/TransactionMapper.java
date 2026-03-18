package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntityMapper;
import com.budget.buddy.budget_buddy_api.generated.model.PaginatedTransactions;
import com.budget.buddy.budget_buddy_api.generated.model.Transaction;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionCreate;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionUpdate;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper
    extends OwnableEntityMapper<TransactionEntity, UUID, Transaction, TransactionCreate, TransactionUpdate, PaginatedTransactions> {
}
