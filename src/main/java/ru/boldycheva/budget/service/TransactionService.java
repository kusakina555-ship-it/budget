package ru.boldycheva.budget.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.boldycheva.budget.entity.Transaction;
import ru.boldycheva.budget.repository.TransactionRepository;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public List<Transaction> getRecentTransactions(int limit) {
        return transactionRepository.findTopNByOrderByTransactionDateDesc(limit);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByTransactionDateDesc();
    }
}