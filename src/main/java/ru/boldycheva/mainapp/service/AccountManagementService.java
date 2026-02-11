package ru.boldycheva.mainapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.boldycheva.mainapp.dto.AccountDataDto;
import ru.boldycheva.mainapp.dto.AccountEditDataDto;
import ru.boldycheva.mainapp.dto.AccountSummaryDto;
import ru.boldycheva.mainapp.dto.EditAccountDto;
import ru.boldycheva.mainapp.entity.Account;
import ru.boldycheva.mainapp.entity.User;
import ru.boldycheva.mainapp.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountManagementService {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository;

    public AccountDataDto getAccountData(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }

        String username = authentication.getName();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        return new AccountDataDto(user.getId(), user.getUserName(), isAdmin);
    }

    public AccountSummaryDto getAccountSummary(Authentication authentication) {
        List<Account> accounts = accountService.getAccountsForCurrentUser(authentication);
        return accountService.getAccountSummary(accounts);
    }

    @Transactional
    public Account createAccountForUser(Long userId, BigDecimal initialBalance, String currency) {
        return accountService.createAccount(userId, initialBalance, currency);
    }

    @Transactional
    public Account createAccountForCurrentUser(Authentication authentication, BigDecimal initialBalance, String currency) {
        return accountService.createAccountForCurrentUser(authentication, initialBalance, currency);
    }

    public AccountEditDataDto getAccountEditData(Long accountId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }

        String username = authentication.getName();
        User currentUser = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        Account account = accountService.getAccountWithPermissionCheck(accountId, currentUser.getId(), isAdmin);

        EditAccountDto editAccountDto = new EditAccountDto();
        editAccountDto.setAccountId(account.getId());
        editAccountDto.setUserId(account.getUser().getId());
        editAccountDto.setBalance(account.getBalance());
        editAccountDto.setCurrency(account.getCurrency());

        return new AccountEditDataDto(account, editAccountDto, isAdmin);
    }

    @Transactional
    public void updateAccountWithPermissionCheck(Long accountId,
                                                 EditAccountDto editAccountDto,
                                                 Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }

        String username = authentication.getName();
        User currentUser = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !editAccountDto.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("Обычные пользователи не могут менять владельца счета");
        }

        accountService.updateAccount(
                editAccountDto.getAccountId(),
                editAccountDto.getUserId(),
                editAccountDto.getBalance(),
                editAccountDto.getCurrency()
        );
    }

    @Transactional
    public void deleteAccountWithPermissionCheck(Long accountId, Authentication authentication) {
        accountService.deleteAccount(accountId, authentication);
    }
}
