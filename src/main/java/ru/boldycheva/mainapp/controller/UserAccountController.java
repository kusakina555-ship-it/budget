package ru.boldycheva.mainapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.boldycheva.mainapp.dto.EditAccountDto;
import ru.boldycheva.mainapp.service.AccountManagementService;

import java.math.BigDecimal;

@Controller
@RequestMapping("/my-accounts")
public class UserAccountController {

    @Autowired
    private AccountManagementService accountManagementService;

    // === СОЗДАНИЕ СЧЕТА ===
    @GetMapping("/new")
    public String showCreateAccountForm(Model model, Authentication authentication) {
        try {
            var accountData = accountManagementService.getAccountData(authentication);
            model.addAttribute("pageTitle", "Создать новый счет");
            model.addAttribute("username", accountData.getUsername());
            model.addAttribute("isAdmin", accountData.isAdmin());
            model.addAttribute("initialBalance", BigDecimal.ZERO); // Добавляем начальный баланс
            model.addAttribute("currency", "RUB"); // Добавляем валюту по умолчанию
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "accounts/user-new";
    }

    @PostMapping("/new")
    public String createAccount(@RequestParam BigDecimal initialBalance,
                                @RequestParam String currency,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            accountManagementService.createAccountForCurrentUser(authentication, initialBalance, currency);
            redirectAttributes.addFlashAttribute("successMessage", "Счет успешно создан!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при создании счета: " + e.getMessage());
        }

        return "redirect:/accounts";
    }

    // === РЕДАКТИРОВАНИЕ СЧЕТА ===
    @GetMapping("/{id}/edit")
    public String showEditAccountForm(@PathVariable Long id,
                                      Authentication authentication,
                                      Model model) {
        try {
            var accountEditData = accountManagementService.getAccountEditData(id, authentication);
            model.addAttribute("account", accountEditData.getAccount());
            model.addAttribute("editAccountDto", accountEditData.getEditAccountDto());
            model.addAttribute("isAdmin", accountEditData.isAdmin());
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/accounts";
        }
        return "accounts/user-edit";
    }

    @PostMapping("/{id}/edit")
    public String updateAccount(@PathVariable Long id,
                                @ModelAttribute EditAccountDto editAccountDto,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            accountManagementService.updateAccountWithPermissionCheck(id, editAccountDto, authentication);
            redirectAttributes.addFlashAttribute("successMessage", "Счет успешно обновлен!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении: " + e.getMessage());
        }
        return "redirect:/accounts";
    }
}