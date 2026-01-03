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
import ru.boldycheva.budget.entity.User;
import ru.boldycheva.budget.service.*;
import ru.boldycheva.budget.entity.TransactionType;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    // Показать форму создания транзакции
    @GetMapping("/new")
    public String showTransactionForm(Model model) {
        model.addAttribute("transactionDto", new TransactionDto());
        model.addAttribute("incomeCategories", categoryService.getTopLevelCategoriesByType("INCOME"));
        model.addAttribute("expenseCategories", categoryService.getTopLevelCategoriesByType("EXPENSE"));
        model.addAttribute("transactionTypes", TransactionType.values());
        model.addAttribute("accounts", accountService.getAllAccounts());
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

        if (bindingResult.hasErrors()) {
            model.addAttribute("incomeCategories", categoryService.getTopLevelCategoriesByType("INCOME"));
            model.addAttribute("expenseCategories", categoryService.getTopLevelCategoriesByType("EXPENSE"));
            model.addAttribute("transactionTypes", TransactionType.values());
            model.addAttribute("accounts", accountService.getAllAccounts());
            return "transactions/new";
        }

        try {
            // Получаем ID текущего пользователя (для простоты берем первого)
            // В реальном приложении нужно получать из authentication
            User currentUser = userService.getAllUsersWithoutPasswords().stream()
                    .findFirst()
                    .map(dto -> {
                        User user = new User();
                        user.setId(dto.getId());
                        return user;
                    })
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            if (transactionDto.getTransactionType().equals("TRANSFER")) {
                transactionService.createTransferTransaction(transactionDto, currentUser.getId());
            } else {
                transactionService.createIncomeExpenseTransaction(transactionDto, currentUser.getId());
            }

            redirectAttributes.addFlashAttribute("successMessage", "Транзакция успешно добавлена!");
            return "redirect:/dashboard";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при создании транзакции: " + e.getMessage());
            model.addAttribute("incomeCategories", categoryService.getTopLevelCategoriesByType("INCOME"));
            model.addAttribute("expenseCategories", categoryService.getTopLevelCategoriesByType("EXPENSE"));
            model.addAttribute("transactionTypes", TransactionType.values());
            model.addAttribute("accounts", accountService.getAllAccounts());
            return "transactions/new";
        }
    }

    // Список всех транзакций
    @GetMapping
    public String getAllTransactions(Model model) {
        model.addAttribute("transactions", transactionService.getAllTransactions());
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
    public String deleteTransaction(@PathVariable Long id, Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            // Получаем ID текущего пользователя
            User currentUser = userService.getAllUsersWithoutPasswords().stream()
                    .findFirst()
                    .map(dto -> {
                        User user = new User();
                        user.setId(dto.getId());
                        return user;
                    })
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            transactionService.deleteTransaction(id, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Транзакция удалена!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении: " + e.getMessage());
        }
        return "redirect:/transactions";
    }
}