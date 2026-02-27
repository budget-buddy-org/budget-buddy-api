package com.budget.buddy.budget_buddy_api.service;

import com.budget.buddy.budget_buddy_api.mapper.TransactionMapper;
import com.budget.buddy.budget_buddy_api.model.Transaction;
import com.budget.buddy.budget_buddy_api.model.TransactionCreate;
import com.budget.buddy.budget_buddy_api.model.TransactionUpdate;
import com.budget.buddy.budget_buddy_api.repository.TransactionRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

  private final TransactionRepository transactionRepository;
  private final TransactionMapper transactionMapper;

  public TransactionService(TransactionRepository transactionRepository, TransactionMapper transactionMapper) {
    this.transactionRepository = transactionRepository;
    this.transactionMapper = transactionMapper;
  }

  public List<Transaction> listTransactions(int limit, int offset, String categoryId, LocalDate start, LocalDate end) {
    var entities = transactionRepository.findByDateRangeAndCategory(start, end, categoryId);
    var endIdx = Math.min(offset + limit, entities.size());
    var page = entities.subList(offset, endIdx);
    return transactionMapper.toTransactions(page);
  }

  public long countTransactions(String categoryId, LocalDate start, LocalDate end) {
    return transactionRepository.findByDateRangeAndCategory(start, end, categoryId).size();
  }

  public Transaction getTransaction(String transactionId) {
    var entity = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
    return transactionMapper.toTransaction(entity);
  }

  @Transactional
  public Transaction createTransaction(TransactionCreate request) {
    var entity = transactionMapper.toEntity(request);

    var saved = transactionRepository.save(entity);

    return transactionMapper.toTransaction(saved);
  }

  @Transactional
  public Transaction updateTransaction(String transactionId, TransactionUpdate request) {
    var entity = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

    entity = transactionMapper.toEntity(request);
    entity.setId(transactionId);

    var saved = transactionRepository.save(entity);
    return transactionMapper.toTransaction(saved);
  }

  @Transactional
  public void deleteTransaction(String transactionId) {
    transactionRepository.deleteById(transactionId);
  }
}
