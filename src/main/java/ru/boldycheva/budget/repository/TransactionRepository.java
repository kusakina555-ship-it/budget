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

    List<Transaction> findAllByOrderByTransactionDateDesc();
}
