package ru.boldycheva.budget.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import ru.boldycheva.budget.entity.Account;
import ru.boldycheva.budget.entity.User;
import ru.boldycheva.budget.repository.AccountRepository;
import ru.boldycheva.budget.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;

    public BigDecimal getTotalBalance() {
        return accountRepository.getTotalBalance();
    }

    // Для админа - все счета, для обычных пользователей - только свои
    public List<Account> getAccountsForCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return List.of();
        }

        String username = authentication.getName();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (isAdmin) {
            // Админ видит все счета
            return accountRepository.findAll();
        } else {
            // Обычный пользователь видит только свои счета
            return accountRepository.findByUserId(user.getId());
        }
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
    public String getCurrencyByAccountId(Long accountId) {
        return accountRepository.findById(accountId)
                .map(Account::getCurrency)
                .orElse("RUB");
    }
}
