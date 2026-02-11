package ru.boldycheva.mainapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.boldycheva.mainapp.dto.AccountSummaryDto;
import ru.boldycheva.mainapp.entity.Account;
import ru.boldycheva.mainapp.entity.User;
import ru.boldycheva.mainapp.repository.AccountRepository;
import ru.boldycheva.mainapp.repository.UserRepository;

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

    // Метод для получения баланса пользователя
    public BigDecimal getUserBalance(Long userId) {
        return accountRepository.getBalanceByUserId(userId);
    }

    // Метод для получения общего баланса по списку счетов
    public BigDecimal calculateTotalBalance(List<Account> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Метод для получения данных для страницы счетов
    public AccountSummaryDto getAccountSummary(List<Account> accounts) {
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

        return new AccountSummaryDto(
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

    // Метод для удаления счета с Authentication
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

        // Получаем счет
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Счет не найден"));

        // Проверяем права доступа
        if (!isAdmin && !account.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Счет не принадлежит текущему пользователю");
        }

        // Проверяем, что баланс равен нулю
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new RuntimeException("Нельзя удалить счет с ненулевым балансом");
        }

        accountRepository.delete(account);
    }

    // Метод для обновления баланса счета
    @Transactional
    public Account updateAccountBalance(Long accountId, BigDecimal newBalance) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Счет не найден"));

        account.setBalance(newBalance);
        return accountRepository.save(account);
    }

    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Счет не найден"));
    }

    @Transactional
    public Account updateAccount(Long accountId, Long newUserId, BigDecimal newBalance, String newCurrency) {
        Account account = getAccountById(accountId);

        // Если меняем владельца
        if (newUserId != null && !account.getUser().getId().equals(newUserId)) {
            User newUser = userRepository.findById(newUserId)
                    .orElseThrow(() -> new RuntimeException("Новый владелец не найден"));
            account.setUser(newUser);
        }

        // Обновляем баланс
        if (newBalance != null) {
            account.setBalance(newBalance);
        }

        // Обновляем валюту
        if (newCurrency != null) {
            account.setCurrency(newCurrency);
        }

        return accountRepository.save(account);
    }

    // Проверка, может ли пользователь редактировать/удалять счет
    public boolean canUserManageAccount(Long accountId, Long userId, boolean isAdmin) {
        if (isAdmin) {
            return true; // Админ может все
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Счет не найден"));

        return account.getUser().getId().equals(userId);
    }

    // Получить счет с проверкой прав
    public Account getAccountWithPermissionCheck(Long accountId, Long userId, boolean isAdmin) {
        Account account = getAccountById(accountId);

        if (!canUserManageAccount(accountId, userId, isAdmin)) {
            throw new RuntimeException("У вас нет прав для управления этим счетом");
        }

        return account;
    }

    @Transactional
    public Account createAccountForCurrentUser(Authentication authentication, BigDecimal initialBalance, String currency) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }

        String username = authentication.getName();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Account account = new Account();
        account.setBalance(initialBalance);
        account.setCurrency(currency);
        account.setUser(user);

        return accountRepository.save(account);
    }

    public List<Account> getAccountsForUser(Long userId, boolean isAdmin) {
        if (isAdmin) {
            return accountRepository.findAll();
        } else {
            return accountRepository.findByUserId(userId);
        }
    }

    public boolean canUserAccessAccount(Long accountId, Long userId, boolean isAdmin) {
        if (isAdmin) {
            return true;
        }

        if (accountId == null) {
            return false;
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Счет не найден"));

        return account.getUser().getId().equals(userId);
    }
}