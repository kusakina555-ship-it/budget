package ru.boldycheva.mainapp.service;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.boldycheva.mainapp.dto.*;
import ru.boldycheva.mainapp.entity.Account;
import ru.boldycheva.mainapp.entity.Category;
import ru.boldycheva.mainapp.entity.TransactionType;
import ru.boldycheva.mainapp.entity.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionControllerService {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionFormService transactionFormService;

    @Autowired
    private TransactionManagementService transactionManagementService;

    @Autowired
    private TransactionService transactionService;

    /**
     * Подготовить данные для формы создания транзакции
     */
    public String prepareTransactionForm(Model model, Authentication authentication, String transactionType) {
        try {
            TransactionFormResponseDto formData = getTransactionFormData(authentication);

            model.addAttribute("transactionDto", new TransactionDto());
            model.addAttribute("incomeCategories", formData.getIncomeCategories());
            model.addAttribute("expenseCategories", formData.getExpenseCategories());
            model.addAttribute("transactionTypes", TransactionType.values());
            model.addAttribute("accounts", formData.getAccounts());
            model.addAttribute("isAdmin", formData.isAdmin());

            // Если передан тип, устанавливаем его в DTO
            if (transactionType != null) {
                TransactionDto dto = new TransactionDto();
                dto.setTransactionType(transactionType);
                model.addAttribute("transactionDto", dto);
            }

            return "transactions/new";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка загрузки формы: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

    /**
     * Обработать создание транзакции
     */
    public String processTransactionCreation(
            @Valid TransactionDto transactionDto,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        // UI валидация
        try {
            validateTransactionForUI(transactionDto);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("error.transactionDto", e.getMessage());
        }

        if (bindingResult.hasErrors()) {
            return reloadFormData(model, authentication, "transactions/new");
        }

        try {
            // Бизнес-логика в сервисе
            String successMessage = transactionManagementService.createTransaction(transactionDto, authentication);
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
            return "redirect:/dashboard";

        } catch (Exception e) {
            reloadFormData(model, authentication, "transactions/new");
            model.addAttribute("errorMessage", e.getMessage());
            return "transactions/new";
        }
    }

    /**
     * Обработать удаление транзакции
     */
    public String processTransactionDeletion(Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            String successMessage = transactionManagementService.deleteTransaction(id, authentication);
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении: " + e.getMessage());
        }
        return "redirect:/transactions";
    }

    /**
     * Получить категории по типу (API)
     */
    public ResponseEntity<ApiResponse<?>> getCategoriesByType(String type, Authentication authentication) {
        try {
            List<Category> topLevelCategories = categoryService.getTopLevelCategoriesByType(type);

            List<CategoryWithSubcategoriesDto> categoryDtos = topLevelCategories.stream()
                    .map(category -> {
                        CategoryWithSubcategoriesDto dto = new CategoryWithSubcategoriesDto();
                        dto.setId(category.getId());
                        dto.setName(category.getName());
                        dto.setCategoryType(category.getCategoryType());

                        List<CategoryDto> subcategoryDtos = category.getSubcategories().stream()
                                .map(subcat -> {
                                    CategoryDto subcatDto = new CategoryDto();
                                    subcatDto.setId(subcat.getId());
                                    subcatDto.setName(subcat.getName());
                                    subcatDto.setCategoryType(subcat.getCategoryType());
                                    subcatDto.setParentId(category.getId());
                                    return subcatDto;
                                })
                                .collect(Collectors.toList());

                        dto.setSubcategories(subcategoryDtos);
                        return dto;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse<>(true, "Success", categoryDtos));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Получить подкатегории (API)
     */
    public ResponseEntity<ApiResponse<?>> getSubcategories(Long parentId, Authentication authentication) {
        try {
            String username = authentication.getName();
            List<CategoryDto> subcategories = transactionFormService.getSubcategories(parentId, username);
            return ResponseEntity.ok(new ApiResponse<>(true, "Success", subcategories));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Создать категорию (API)
     */
    public ResponseEntity<ApiResponse<?>> createCategory(CategoryDto categoryDto, Authentication authentication) {
        try {
            String username = authentication.getName();
            Category category = transactionFormService.createCategoryFromTransactionForm(categoryDto, username);

            CategoryDto responseDto = new CategoryDto();
            responseDto.setId(category.getId());
            responseDto.setName(category.getName());
            responseDto.setCategoryType(category.getCategoryType());
            if (category.getParent() != null) {
                responseDto.setParentId(category.getParent().getId());
            }

            return ResponseEntity.ok(new ApiResponse<>(true, "Категория создана", responseDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    // ============ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ============

    /**
     * Получить данные для формы создания транзакции
     */
    private TransactionFormResponseDto getTransactionFormData(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }

        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        boolean isAdmin = user.getRoles().contains("ADMIN");

        // Получаем категории
        List<Category> incomeCategories = categoryService.getTopLevelCategoriesByType("INCOME");
        List<Category> expenseCategories = categoryService.getTopLevelCategoriesByType("EXPENSE");

        // Получаем счета
        List<Account> accounts = accountService.getAccountsForUser(user.getId(), isAdmin);

        return new TransactionFormResponseDto(incomeCategories, expenseCategories, accounts, isAdmin);
    }

    /**
     * Валидация UI логики для транзакций
     */
    private void validateTransactionForUI(TransactionDto transactionDto) {
        if ("TRANSFER".equals(transactionDto.getTransactionType())) {
            if (transactionDto.getFromAccountId() == null) {
                throw new IllegalArgumentException("Выберите счет списания");
            }
            if (transactionDto.getToAccountId() == null) {
                throw new IllegalArgumentException("Выберите счет зачисления");
            }
            if (transactionDto.getFromAccountId() != null &&
                    transactionDto.getToAccountId() != null &&
                    transactionDto.getFromAccountId().equals(transactionDto.getToAccountId())) {
                throw new IllegalArgumentException("Нельзя перевести средства на тот же счет");
            }
        } else {
            if (transactionDto.getAccountId() == null) {
                throw new IllegalArgumentException("Выберите счет");
            }
            if (transactionDto.getCategoryId() == null) {
                throw new IllegalArgumentException("Выберите категорию");
            }
        }
    }

    /**
     * Перезагрузить данные формы
     */
    private String reloadFormData(Model model, Authentication authentication, String viewName) {
        try {
            TransactionFormResponseDto formData = getTransactionFormData(authentication);
            model.addAttribute("incomeCategories", formData.getIncomeCategories());
            model.addAttribute("expenseCategories", formData.getExpenseCategories());
            model.addAttribute("transactionTypes", TransactionType.values());
            model.addAttribute("accounts", formData.getAccounts());
            model.addAttribute("isAdmin", formData.isAdmin());
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка загрузки данных: " + e.getMessage());
        }
        return viewName;
    }
}
