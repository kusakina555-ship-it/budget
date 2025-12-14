package ru.boldycheva.budget.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.boldycheva.budget.entity.Account;
import java.math.BigDecimal;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a")
    BigDecimal getTotalBalance();
}
