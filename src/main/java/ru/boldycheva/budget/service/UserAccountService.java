package ru.boldycheva.budget.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.boldycheva.budget.dto.*;
import ru.boldycheva.budget.entity.Account;
import ru.boldycheva.budget.entity.User;
import ru.boldycheva.budget.repository.UserRepository;

import java.math.BigDecimal;

@Service
public class UserAccountService {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Получить данные для создания нового счета
     */
    public AccountDataDto getCreateAccountData(Authentication authentication) {
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

    /**
     * Создать новый счет для текущего пользователя
     */
    @Transactional
    public void createAccountForCurrentUser(Authentication authentication,
                                            BigDecimal initialBalance,
                                            String currency) {
        accountService.createAccountForCurrentUser(authentication, initialBalance, currency);
    }

    /**
     * Получить данные для редактирования счета с проверкой прав
     */
    public AccountEditDataDto getAccountEditData(Long accountId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }

        String username = authentication.getName();
        User currentUser = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        // Получаем счет с проверкой прав
        Account account = accountService.getAccountWithPermissionCheck(accountId, currentUser.getId(), isAdmin);

        // Создаем DTO для формы
        EditAccountDto editAccountDto = new EditAccountDto();
        editAccountDto.setAccountId(account.getId());
        editAccountDto.setUserId(account.getUser().getId());
        editAccountDto.setBalance(account.getBalance());
        editAccountDto.setCurrency(account.getCurrency());

        return new AccountEditDataDto(account, editAccountDto, isAdmin);
    }

    /**
     * Обновить счет с проверкой прав
     */
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

        // Проверяем права доступа
        if (!isAdmin && !editAccountDto.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("Обычные пользователи не могут менять владельца счета");
        }

        // Обновляем счет
        accountService.updateAccount(
                editAccountDto.getAccountId(),
                editAccountDto.getUserId(),
                editAccountDto.getBalance(),
                editAccountDto.getCurrency()
        );
    }

    /**
     * Удалить счет с проверкой прав
     */
    @Transactional
    public void deleteAccountWithPermissionCheck(Long accountId, Authentication authentication) {
        accountService.deleteAccount(accountId, authentication);
    }
}