package ru.boldycheva.budget.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.boldycheva.budget.entity.Transaction;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t ORDER BY t.transactionDate DESC LIMIT :limit")
    List<Transaction> findTopNByOrderByTransactionDateDesc(@Param("limit") int limit);

    @Query("SELECT t FROM Transaction t ORDER BY t.transactionDate DESC")
    List<Transaction> findAllByOrderByTransactionDateDesc();

    // Найти все переводы для счета
    @Query("SELECT t FROM Transaction t WHERE t.account.id = :accountId AND t.transactionType = 'TRANSFER'")
    List<Transaction> findTransfersByAccountId(@Param("accountId") Long accountId);

    // Найти связанные транзакции
    @Query("SELECT t FROM Transaction t WHERE t.relatedTransactionId = :relatedId OR t.id = :relatedId")
    List<Transaction> findRelatedTransactions(@Param("relatedId") Long relatedId);
}