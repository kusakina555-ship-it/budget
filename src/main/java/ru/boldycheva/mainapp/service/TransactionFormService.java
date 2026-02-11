package ru.boldycheva.mainapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.boldycheva.mainapp.dto.*;
import ru.boldycheva.mainapp.entity.Account;
import ru.boldycheva.mainapp.entity.Category;
import ru.boldycheva.mainapp.entity.TransactionType;
import ru.boldycheva.mainapp.entity.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionFormService {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    /**
     * Получить все данные для формы создания транзакции
     */
    public TransactionFormDataDto getTransactionFormData(String username) {
        // Получаем текущего пользователя
        User currentUser = userService.getUserByUsername(username);
        boolean isAdmin = currentUser.getRoles().contains("ADMIN");

        // Получаем категории
        List<Category> incomeCategories = categoryService.getTopLevelCategoriesByType("INCOME");
        List<Category> expenseCategories = categoryService.getTopLevelCategoriesByType("EXPENSE");

        // Получаем счета
        List<Account> accounts = accountService.getAccountsForUser(currentUser.getId(), isAdmin);

        return new TransactionFormDataDto(
                incomeCategories,
                expenseCategories,
                TransactionType.values(),
                accounts,
                isAdmin
        );
    }

    /**
     * Получить категории для конкретного типа транзакции
     * с учетом пользователя
     */
    public List<CategoryDto> getCategoriesForTransactionType(String transactionType, String username) {
        User user = userService.getUserByUsername(username);
        boolean isAdmin = user.getRoles().contains("ADMIN");

        String categoryType = mapTransactionTypeToCategoryType(transactionType);
        List<Category> categories = categoryService.getTopLevelCategoriesByType(categoryType);

        return categories.stream()
                .map(category -> {
                    CategoryDto dto = new CategoryDto();
                    dto.setId(category.getId());
                    dto.setName(category.getName());
                    dto.setCategoryType(category.getCategoryType());
                    if (category.getParent() != null) {
                        dto.setParentId(category.getParent().getId());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Получить подкатегории с учетом пользователя
     */
    public List<CategoryDto> getSubcategories(Long parentId, String username) {
        User user = userService.getUserByUsername(username);
        boolean isAdmin = user.getRoles().contains("ADMIN");

        List<Category> subcategories = categoryService.getSubcategories(parentId);

        return subcategories.stream()
                .map(category -> {
                    CategoryDto dto = new CategoryDto();
                    dto.setId(category.getId());
                    dto.setName(category.getName());
                    dto.setCategoryType(category.getCategoryType());
                    dto.setParentId(parentId);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Создать новую категорию из формы транзакции
     */
    public Category createCategoryFromTransactionForm(CategoryDto categoryDto, String username) {
        User user = userService.getUserByUsername(username);
        boolean isAdmin = user.getRoles().contains("ADMIN");

        // Проверяем, может ли пользователь создавать категории
        validateCategoryCreationPermissions(categoryDto, isAdmin);

        return categoryService.createCategory(
                categoryDto.getName(),
                categoryDto.getCategoryType(),
                categoryDto.getParentId(),
                user
        );
    }

    /**
     * Подготовить данные для валидации транзакции
     */
    public Map<String, Object> prepareTransactionValidation(TransactionDto transactionDto, String username) {
        Map<String, Object> validationData = new HashMap<>();

        User user = userService.getUserByUsername(username);
        boolean isAdmin = user.getRoles().contains("ADMIN");

        if ("TRANSFER".equals(transactionDto.getTransactionType())) {
            // Проверяем доступ к счетам для переводов
            validationData.put("hasAccessToFromAccount",
                    accountService.canUserAccessAccount(transactionDto.getFromAccountId(), user.getId(), isAdmin));
            validationData.put("hasAccessToToAccount",
                    accountService.canUserAccessAccount(transactionDto.getToAccountId(), user.getId(), isAdmin));
        } else {
            // Проверяем доступ к счету для доходов/расходов
            validationData.put("hasAccessToAccount",
                    accountService.canUserAccessAccount(transactionDto.getAccountId(), user.getId(), isAdmin));
            validationData.put("categoryExists",
                    categoryService.categoryExists(transactionDto.getCategoryId()));
        }

        return validationData;
    }

    /**
     * Получить URL для возврата после создания категории
     */
    public String getReturnUrl(String transactionType) {
        return "/transactions/new?type=" + transactionType;
    }

    // Вспомогательные методы
    private String mapTransactionTypeToCategoryType(String transactionType) {
        return switch (transactionType) {
            case "INCOME" -> "INCOME";
            case "EXPENSE" -> "EXPENSE";
            default -> throw new IllegalArgumentException("Неподдерживаемый тип транзакции: " + transactionType);
        };
    }

    private void validateCategoryCreationPermissions(CategoryDto categoryDto, boolean isAdmin) {
        // Если создаем категорию верхнего уровня - только админ
        if (categoryDto.getParentId() == null && !isAdmin) {
            throw new SecurityException("Только администратор может создавать категории верхнего уровня");
        }
    }
}
