package ru.boldycheva.budget.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.boldycheva.budget.service.AccountService;

@Controller
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping
    public String getUserAccounts(Model model, Authentication authentication) {
        // Получаем счета через сервис
        var accounts = accountService.getAccountsForCurrentUser(authentication);

        // Получаем сводную информацию через сервис
        var summary = accountService.getAccountSummary(accounts);

        // Добавляем данные в модель
        model.addAttribute("accounts", accounts);
        model.addAttribute("pageTitle", "Мои счета");
        model.addAttribute("isAdminPage", false);
        model.addAttribute("totalBalance", summary.getTotalBalance());
        model.addAttribute("accountSummary", summary);

        return "accounts/list";
    }
}
