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
import ru.boldycheva.budget.repository.UserRepository;

@Controller
@RequestMapping("/transactions")
public class TransactionController {
    @Autowired
    private UserRepository userRepository;

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
    public String showTransactionForm(Model model, Authentication authentication) {
        model.addAttribute("transactionDto", new TransactionDto());
        model.addAttribute("incomeCategories", categoryService.getTopLevelCategoriesByType("INCOME"));
        model.addAttribute("expenseCategories", categoryService.getTopLevelCategoriesByType("EXPENSE"));
        model.addAttribute("transactionTypes", TransactionType.values());
        model.addAttribute("accounts", accountService.getAccountsForCurrentUser(authentication));
        model.addAttribute("isAdmin", isAdmin(authentication));
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
            model.addAttribute("incomeCategories", categoryService.getTopLevelCategoriesByType("INCOME"));
            model.addAttribute("expenseCategories", categoryService.getTopLevelCategoriesByType("EXPENSE"));
            model.addAttribute("transactionTypes", TransactionType.values());
            model.addAttribute("accounts", accountService.getAccountsForCurrentUser(authentication));
            model.addAttribute("isAdmin", isAdmin(authentication));
            return "transactions/new";
        }

        try {
            // Получаем текущего пользователя из Authentication
            String username = authentication.getName();
            User currentUser = userRepository.findByUserName(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // Проверяем, является ли пользователь админом
            boolean isAdmin = isAdmin(authentication);

            if ("TRANSFER".equals(transactionDto.getTransactionType())) {
                transactionService.createTransferTransaction(transactionDto, currentUser.getId(), isAdmin);
            } else {
                transactionService.createIncomeExpenseTransaction(transactionDto, currentUser.getId(), isAdmin);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Транзакция успешно добавлена!");
            return "redirect:/dashboard";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при создании транзакции: " + e.getMessage());
            model.addAttribute("incomeCategories", categoryService.getTopLevelCategoriesByType("INCOME"));
            model.addAttribute("expenseCategories", categoryService.getTopLevelCategoriesByType("EXPENSE"));
            model.addAttribute("transactionTypes", TransactionType.values());
            model.addAttribute("accounts", accountService.getAccountsForCurrentUser(authentication));
            model.addAttribute("isAdmin", isAdmin(authentication));
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
            String username = authentication.getName();
            User currentUser = userRepository.findByUserName(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            boolean isAdmin = isAdmin(authentication);

            transactionService.deleteTransaction(id, currentUser.getId(), isAdmin);
            redirectAttributes.addFlashAttribute("successMessage", "Транзакция удалена!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении: " + e.getMessage());
        }
        return "redirect:/transactions";
    }

    // Вспомогательный метод для проверки роли ADMIN
    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}