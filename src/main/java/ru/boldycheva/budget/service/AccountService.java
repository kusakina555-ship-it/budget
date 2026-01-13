package ru.boldycheva.budget.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
            return accountRepository.findAll();
        } else {
            return accountRepository.findByUserId(user.getId());
        }
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Transactional
    public Account createAccount(Long userId, BigDecimal initialBalance, String currency) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Account account = new Account();
        account.setBalance(initialBalance);
        account.setCurrency(currency);
        account.setUser(user);

        return accountRepository.save(account);
    }

    public String getCurrencyByAccountId(Long accountId) {
        return accountRepository.findById(accountId)
                .map(Account::getCurrency)
                .orElse("RUB");
    }

    // Новый метод для получения баланса пользователя
    public BigDecimal getUserBalance(Long userId) {
        return accountRepository.getBalanceByUserId(userId);
    }

    // Новый метод для получения общего баланса по списку счетов
    public BigDecimal calculateTotalBalance(List<Account> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Новый метод для получения данных для страницы счетов
    public AccountSummary getAccountSummary(List<Account> accounts) {
        BigDecimal totalBalance = calculateTotalBalance(accounts);

        long rubCount = accounts.stream()
                .filter(acc -> "RUB".equals(acc.getCurrency()))
                .count();

        long usdCount = accounts.stream()
                .filter(acc -> "USD".equals(acc.getCurrency()))
                .count();

        long positiveBalanceCount = accounts.stream()
                .filter(acc -> acc.getBalance().compareTo(BigDecimal.ZERO) > 0)
                .count();

        long zeroBalanceCount = accounts.stream()
                .filter(acc -> acc.getBalance().compareTo(BigDecimal.ZERO) == 0)
                .count();

        // Суммы по валютам
        BigDecimal rubTotal = accounts.stream()
                .filter(acc -> "RUB".equals(acc.getCurrency()))
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal usdTotal = accounts.stream()
                .filter(acc -> "USD".equals(acc.getCurrency()))
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AccountSummary(
                totalBalance,
                accounts.size(),
                positiveBalanceCount,
                zeroBalanceCount,
                rubCount,
                usdCount,
                rubTotal,
                usdTotal
        );
    }

    // Удалить счет (старый метод для совместимости)
    @Transactional
    public void deleteAccount(Long accountId, Long userId, boolean isAdmin) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Счет не найден"));

        // Проверяем права доступа
        if (!isAdmin && !account.getUser().getId().equals(userId)) {
            throw new RuntimeException("Счет не принадлежит текущему пользователю");
        }

        // Проверяем, что баланс равен нулю
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new RuntimeException("Нельзя удалить счет с ненулевым балансом");
        }

        accountRepository.delete(account);
    }

    // Новый метод для удаления счета с Authentication
    @Transactional
    public void deleteAccount(Long accountId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }

        String username = authentication.getName();
        User currentUser = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        // Вызываем существующий метод
        deleteAccount(accountId, currentUser.getId(), isAdmin);
    }

    // Метод для обновления баланса счета
    @Transactional
    public Account updateAccountBalance(Long accountId, BigDecimal newBalance) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Счет не найден"));

        account.setBalance(newBalance);
        return accountRepository.save(account);
    }

    // DTO для передачи сводной информации о счетах
    public static class AccountSummary {
        private final BigDecimal totalBalance;
        private final int totalAccounts;
        private final long positiveBalanceAccounts;
        private final long zeroBalanceAccounts;
        private final long rubAccounts;
        private final long usdAccounts;
        private final BigDecimal rubTotal;
        private final BigDecimal usdTotal;

        public AccountSummary(BigDecimal totalBalance, int totalAccounts,
                              long positiveBalanceAccounts, long zeroBalanceAccounts,
                              long rubAccounts, long usdAccounts,
                              BigDecimal rubTotal, BigDecimal usdTotal) {
            this.totalBalance = totalBalance;
            this.totalAccounts = totalAccounts;
            this.positiveBalanceAccounts = positiveBalanceAccounts;
            this.zeroBalanceAccounts = zeroBalanceAccounts;
            this.rubAccounts = rubAccounts;
            this.usdAccounts = usdAccounts;
            this.rubTotal = rubTotal;
            this.usdTotal = usdTotal;
        }

        // Геттеры
        public BigDecimal getTotalBalance() { return totalBalance; }
        public int getTotalAccounts() { return totalAccounts; }
        public long getPositiveBalanceAccounts() { return positiveBalanceAccounts; }
        public long getZeroBalanceAccounts() { return zeroBalanceAccounts; }
        public long getRubAccounts() { return rubAccounts; }
        public long getUsdAccounts() { return usdAccounts; }
        public BigDecimal getRubTotal() { return rubTotal; }
        public BigDecimal getUsdTotal() { return usdTotal; }
    }
}