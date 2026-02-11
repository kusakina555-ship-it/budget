package ru.boldycheva.mainapp.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ru.boldycheva.mainapp.dto.TransactionDto;
import ru.boldycheva.mainapp.dto.TransactionFormDataDto;
import ru.boldycheva.mainapp.entity.TransactionType;
import ru.boldycheva.mainapp.entity.User;
import ru.boldycheva.mainapp.repository.UserRepository;

@Service
public class TransactionManagementService {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AccountService accountService;

    @Transactional
    public String createTransaction(TransactionDto transactionDto, Authentication authentication) {
        try {
            String username = authentication.getName();
            User currentUser = userRepository.findByUserName(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            boolean isAdmin = isAdmin(authentication);

            if ("TRANSFER".equals(transactionDto.getTransactionType())) {
                transactionService.createTransferTransaction(transactionDto, currentUser.getId(), isAdmin);
            } else {
                transactionService.createIncomeExpenseTransaction(transactionDto, currentUser.getId(), isAdmin);
            }

            return "Транзакция успешно добавлена!";

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании транзакции: " + e.getMessage(), e);
        }
    }

    @Transactional
    public String deleteTransaction(Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            User currentUser = userRepository.findByUserName(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            boolean isAdmin = isAdmin(authentication);
            transactionService.deleteTransaction(id, currentUser.getId(), isAdmin);
            return "Транзакция удалена!";
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при удалении: " + e.getMessage(), e);
        }
    }

    public TransactionFormDataDto getTransactionFormData(Authentication authentication) {
        return new TransactionFormDataDto(
                categoryService.getTopLevelCategoriesByType("INCOME"),
                categoryService.getTopLevelCategoriesByType("EXPENSE"),
                TransactionType.values(),
                accountService.getAccountsForCurrentUser(authentication),
                isAdmin(authentication)
        );
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}
