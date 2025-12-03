package ru.boldycheva.budget.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.boldycheva.budget.entity.Account;
import ru.boldycheva.budget.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public BigDecimal getTotalBalance() {
        return accountRepository.getTotalBalance();
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account createAccount(BigDecimal initialBalance, String currency) {
        Account account = new Account();
        account.setBalance(initialBalance);
        account.setCurrency(currency);
        return accountRepository.save(account);
    }
}
