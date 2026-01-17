package ru.boldycheva.budget.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.boldycheva.budget.dto.EditAccountDto;
import ru.boldycheva.budget.repository.UserRepository;
import ru.boldycheva.budget.service.AccountService;
import ru.boldycheva.budget.service.UserAccountService;

import java.math.BigDecimal;

@Controller
@RequestMapping("/my-accounts")
public class UserAccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAccountService userAccountService;

    // === СОЗДАНИЕ СЧЕТА ===

    // Показать форму создания счета
    @GetMapping("/new")
    public String showCreateAccountForm(Model model, Authentication authentication) {
        try {
            var accountData = userAccountService.getCreateAccountData(authentication);
            model.addAttribute("pageTitle", "Создать новый счет");
            model.addAttribute("username", accountData.getUsername());
            model.addAttribute("isAdmin", accountData.isAdmin());
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "accounts/user-new";
    }


    // Обработать создание счета
    @PostMapping("/new")
    public String createAccount(@RequestParam BigDecimal initialBalance,
                                @RequestParam String currency,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            accountService.createAccountForCurrentUser(authentication, initialBalance, currency);
            redirectAttributes.addFlashAttribute("successMessage", "Счет успешно создан!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при создании счета: " + e.getMessage());
        }

        return "redirect:/accounts";
    }

    // === РЕДАКТИРОВАНИЕ СЧЕТА ===

    // Показать форму редактирования счета
    @GetMapping("/{id}/edit")
    public String showEditAccountForm(@PathVariable Long id,
                                      Authentication authentication,
                                      Model model) {
        try {
            var accountEditData = userAccountService.getAccountEditData(id, authentication);
            model.addAttribute("account", accountEditData.getAccount());
            model.addAttribute("editAccountDto", accountEditData.getEditAccountDto());
            model.addAttribute("isAdmin", accountEditData.isAdmin());
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/accounts";
        }
        return "accounts/user-edit";
    }

    // Обновить счет
    @PostMapping("/{id}/edit")
    public String updateAccount(@PathVariable Long id,
                                @ModelAttribute EditAccountDto editAccountDto,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            userAccountService.updateAccountWithPermissionCheck(id, editAccountDto, authentication);
            redirectAttributes.addFlashAttribute("successMessage", "Счет успешно обновлен!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении: " + e.getMessage());
        }
        return "redirect:/accounts";
    }

    // === УДАЛЕНИЕ СЧЕТА ===
    // Удалить счет (теперь используется AccountManagementController)
}