package ru.boldycheva.budget.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.boldycheva.budget.dto.*;
import ru.boldycheva.budget.entity.Category;
import ru.boldycheva.budget.service.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionFormService transactionFormService;

    @Autowired
    private TransactionManagementService transactionManagementService;
    @Autowired
    private CategoryService categoryService;

    // === HTML СТРАНИЦЫ ===

    @GetMapping("/new")
    public String showTransactionForm(
            @RequestParam(value = "type", required = false) String transactionType,
            Model model,
            Authentication authentication) {

        // Получаем данные формы из сервиса
        String username = authentication.getName();
        TransactionFormDataDto formData = transactionFormService.getTransactionFormData(username);

        model.addAttribute("transactionDto", new TransactionDto());
        model.addAttribute("incomeCategories", formData.getIncomeCategories());
        model.addAttribute("expenseCategories", formData.getExpenseCategories());
        model.addAttribute("transactionTypes", formData.getTransactionTypes());
        model.addAttribute("accounts", formData.getAccounts());
        model.addAttribute("isAdmin", formData.isAdmin());

        // Если передан тип, устанавливаем его в DTO
        if (transactionType != null) {
            TransactionDto dto = new TransactionDto();
            dto.setTransactionType(transactionType);
            model.addAttribute("transactionDto", dto);
        }

        return "transactions/new";
    }

    @PostMapping("/new")
    public String createTransaction(
            @Valid @ModelAttribute TransactionDto transactionDto,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        // UI валидация (остается в контроллере как вспомогательный метод)
        validateTransactionForUI(transactionDto, bindingResult);

        if (bindingResult.hasErrors()) {
            // Перезагружаем данные формы
            String username = authentication.getName();
            TransactionFormDataDto formData = transactionFormService.getTransactionFormData(username);
            model.addAttribute("incomeCategories", formData.getIncomeCategories());
            model.addAttribute("expenseCategories", formData.getExpenseCategories());
            model.addAttribute("transactionTypes", formData.getTransactionTypes());
            model.addAttribute("accounts", formData.getAccounts());
            model.addAttribute("isAdmin", formData.isAdmin());
            return "transactions/new";
        }

        try {
            // Бизнес-логика в сервисе
            String successMessage = transactionManagementService.createTransaction(transactionDto, authentication);
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
            return "redirect:/dashboard";

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            String username = authentication.getName();
            TransactionFormDataDto formData = transactionFormService.getTransactionFormData(username);
            model.addAttribute("incomeCategories", formData.getIncomeCategories());
            model.addAttribute("expenseCategories", formData.getExpenseCategories());
            model.addAttribute("transactionTypes", formData.getTransactionTypes());
            model.addAttribute("accounts", formData.getAccounts());
            model.addAttribute("isAdmin", formData.isAdmin());
            return "transactions/new";
        }
    }

    @GetMapping
    public String getAllTransactions(Model model) {
        model.addAttribute("transactions", transactionService.getAllTransactionsForDisplay());
        return "transactions/list";
    }

    @GetMapping("/{id}")
    public String getTransactionDetails(@PathVariable Long id, Model model) {
        model.addAttribute("transaction", transactionService.getTransactionById(id));
        return "transactions/details";
    }

    @PostMapping("/{id}/delete")
    public String deleteTransaction(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            String successMessage = transactionManagementService.deleteTransaction(id, authentication);
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении: " + e.getMessage());
        }
        return "redirect:/transactions";
    }

    // === API ENDPOINTS (для AJAX) ===

    @GetMapping("/api/categories/type/{type}")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<CategoryWithSubcategoriesDto>>> getCategoriesByType(
            @PathVariable String type,
            Authentication authentication) {

        try {
            String username = authentication.getName();

            // Получаем категории верхнего уровня
            List<Category> topLevelCategories = categoryService.getTopLevelCategoriesByType(type);

            // Создаем DTO с подкатегориями
            List<CategoryWithSubcategoriesDto> categoryDtos = topLevelCategories.stream()
                    .map(category -> {
                        CategoryWithSubcategoriesDto dto = new CategoryWithSubcategoriesDto();
                        dto.setId(category.getId());
                        dto.setName(category.getName());
                        dto.setCategoryType(category.getCategoryType());

                        // Добавляем подкатегории
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

    @GetMapping("/api/categories/{parentId}/subcategories")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getSubcategories(
            @PathVariable Long parentId,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            List<CategoryDto> subcategories = transactionFormService.getSubcategories(parentId, username);
            return ResponseEntity.ok(new ApiResponse<>(true, "Success", subcategories));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/api/categories")
    @ResponseBody
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(
            @RequestBody CategoryDto categoryDto,
            Authentication authentication) {

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

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    /**
     * Валидация UI логики для транзакций
     * Оставлена в контроллере как вспомогательный метод
     */
    public void validateTransactionForUI(TransactionDto transactionDto, BindingResult bindingResult) {
        if ("TRANSFER".equals(transactionDto.getTransactionType())) {
            if (transactionDto.getFromAccountId() == null) {
                bindingResult.rejectValue("fromAccountId", "error.transactionDto", "Выберите счет списания");
            }
            if (transactionDto.getToAccountId() == null) {
                bindingResult.rejectValue("toAccountId", "error.transactionDto", "Выберите счет зачисления");
            }
            if (transactionDto.getFromAccountId() != null &&
                    transactionDto.getToAccountId() != null &&
                    transactionDto.getFromAccountId().equals(transactionDto.getToAccountId())) {
                bindingResult.rejectValue("toAccountId", "error.transactionDto",
                        "Нельзя перевести средства на тот же счет");
            }
        } else {
            if (transactionDto.getAccountId() == null) {
                bindingResult.rejectValue("accountId", "error.transactionDto", "Выберите счет");
            }
            if (transactionDto.getCategoryId() == null) {
                bindingResult.rejectValue("categoryId", "error.transactionDto", "Выберите категорию");
            }
        }
    }
}