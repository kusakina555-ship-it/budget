package ru.boldycheva.mainapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.boldycheva.mainapp.service.AccountManagementService;
import ru.boldycheva.mainapp.service.AccountService;

@Controller
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountManagementService accountManagementService;

    @GetMapping
    public String getUserAccounts(Model model, Authentication authentication) {
        var accounts = accountService.getAccountsForCurrentUser(authentication);
        var summary = accountManagementService.getAccountSummary(authentication);

        model.addAttribute("accounts", accounts);
        model.addAttribute("pageTitle", "Мои счета");
        model.addAttribute("isAdminPage", false);
        model.addAttribute("totalBalance", summary.getTotalBalance());
        model.addAttribute("accountSummary", summary);

        return "accounts/list";
    }
}
