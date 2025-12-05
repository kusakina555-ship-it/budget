package ru.boldycheva.budget.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.boldycheva.budget.entity.Transaction;
import ru.boldycheva.budget.service.AccountService;
import ru.boldycheva.budget.service.TransactionService;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    @GetMapping({"/", "/dashboard"})
    public String home(Model model, Principal principal) {
        return prepareDashboardModel(model, principal, "home");
    }


    private String prepareDashboardModel(Model model, Principal principal, String viewName) {
        if (principal != null) {
            // Добавляем имя пользователя
            String username = principal.getName();
            model.addAttribute("username", username);

            // Последние 3 транзакции
            try {
                List<Transaction> recentTransactions = transactionService.getRecentTransactions(3);
                model.addAttribute("recentTransactions", recentTransactions);
            } catch (Exception e) {
                // Если транзакций нет или сервис недоступен, передаем пустой список
                model.addAttribute("recentTransactions", List.of());
            }

            // Общий баланс
            try {
                BigDecimal totalBalance = accountService.getTotalBalance();
                model.addAttribute("totalBalance", totalBalance);
            } catch (Exception e) {
                // Если баланс недоступен, используем 0
                model.addAttribute("totalBalance", BigDecimal.ZERO);
            }

            // Проверяем является ли пользователь админом
            boolean isAdmin = username.equals("admin");
            model.addAttribute("isAdmin", isAdmin);
        } else {
            // Если пользователь не авторизован
            model.addAttribute("recentTransactions", List.of());
            model.addAttribute("totalBalance", BigDecimal.ZERO);
            model.addAttribute("isAdmin", false);
            model.addAttribute("username", "Гость");
        }
        return viewName;
    }
}
