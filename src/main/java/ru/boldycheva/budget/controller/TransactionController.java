package ru.boldycheva.budget.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.boldycheva.budget.dto.TransactionDto;
import ru.boldycheva.budget.entity.TransactionType;
import ru.boldycheva.budget.service.TransactionManagementService;
import ru.boldycheva.budget.service.TransactionService;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionManagementService transactionManagementService;

    @Autowired
    private TransactionService transactionService;

    // Показать форму создания транзакции
    @GetMapping("/new")
    public String showTransactionForm(Model model, Authentication authentication) {
        var formData = transactionManagementService.getTransactionFormData(authentication);

        model.addAttribute("transactionDto", new TransactionDto());
        model.addAttribute("incomeCategories", formData.getIncomeCategories());
        model.addAttribute("expenseCategories", formData.getExpenseCategories());
        model.addAttribute("transactionTypes", TransactionType.values()); // Используем напрямую
        model.addAttribute("accounts", formData.getAccounts());
        model.addAttribute("isAdmin", formData.isAdmin());

        return "transactions/new";
    }

    // Обработка создания транзакции
    @PostMapping("/new")
    public String createTransaction(
            @Valid @ModelAttribute TransactionDto transactionDto,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Валидация в зависимости от типа транзакции
        if ("TRANSFER".equals(transactionDto.getTransactionType())) {
            // Для переводов проверяем счета
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
            // Для доходов/расходов проверяем счет и категорию
            if (transactionDto.getAccountId() == null) {
                bindingResult.rejectValue("accountId", "error.transactionDto", "Выберите счет");
            }
            if (transactionDto.getCategoryId() == null) {
                bindingResult.rejectValue("categoryId", "error.transactionDto", "Выберите категорию");
            }
        }

        if (bindingResult.hasErrors()) {
            // Если есть ошибки валидации, показываем форму снова
            var formData = transactionManagementService.getTransactionFormData(authentication);
            model.addAttribute("incomeCategories", formData.getIncomeCategories());
            model.addAttribute("expenseCategories", formData.getExpenseCategories());
            model.addAttribute("transactionTypes", TransactionType.values()); // Используем напрямую
            model.addAttribute("accounts", formData.getAccounts());
            model.addAttribute("isAdmin", formData.isAdmin());
            return "transactions/new";
        }

        try {
            String successMessage = transactionManagementService.createTransaction(transactionDto, authentication);
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
            return "redirect:/dashboard";

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            var formData = transactionManagementService.getTransactionFormData(authentication);
            model.addAttribute("incomeCategories", formData.getIncomeCategories());
            model.addAttribute("expenseCategories", formData.getExpenseCategories());
            model.addAttribute("transactionTypes", TransactionType.values()); // Используем напрямую
            model.addAttribute("accounts", formData.getAccounts());
            model.addAttribute("isAdmin", formData.isAdmin());
            return "transactions/new";
        }
    }

    // Список всех транзакций
    @GetMapping
    public String getAllTransactions(Model model) {
        model.addAttribute("transactions", transactionService.getAllTransactionsForDisplay());
        return "transactions/list";
    }

    // Показать детали транзакции
    @GetMapping("/{id}")
    public String getTransactionDetails(@PathVariable Long id, Model model) {
        model.addAttribute("transaction", transactionService.getTransactionById(id));
        return "transactions/details";
    }

    // Удалить транзакцию
    @PostMapping("/{id}/delete")
    public String deleteTransaction(@PathVariable Long id,
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
}