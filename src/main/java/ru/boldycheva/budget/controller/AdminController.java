package ru.boldycheva.budget.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.boldycheva.budget.dto.CategoryDto;
import ru.boldycheva.budget.dto.EditAccountDto;
import ru.boldycheva.budget.service.AccountManagementService;
import ru.boldycheva.budget.service.AccountService;
import ru.boldycheva.budget.service.CategoryService;
import ru.boldycheva.budget.service.UserService;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountManagementService accountManagementService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/users")
    public String userManagement(Model model) {
        model.addAttribute("users", userService.getAllUsersWithoutPasswords());
        return "admin/users";
    }

    @GetMapping("/accounts")
    public String manageAccounts(Model model) {
        var accounts = accountService.getAllAccounts();
        var summary = accountService.getAccountSummary(accounts);

        model.addAttribute("accounts", accounts);
        model.addAttribute("users", userService.getAllUsersWithoutPasswords());
        model.addAttribute("pageTitle", "Управление счетами");
        model.addAttribute("isAdminPage", true);
        model.addAttribute("totalBalance", summary.getTotalBalance());
        model.addAttribute("accountSummary", summary);

        return "accounts/list";
    }

    @PostMapping("/accounts")
    public String createAccount(@RequestParam Long userId,
                                @RequestParam BigDecimal initialBalance,
                                @RequestParam String currency) {
        accountManagementService.createAccountForUser(userId, initialBalance, currency);
        return "redirect:/admin/accounts";
    }

    @GetMapping("/accounts/{id}/edit")
    public String showEditAccountForm(@PathVariable Long id, Model model) {
        var account = accountService.getAccountById(id);
        var users = userService.getAllUsersWithoutPasswords();

        EditAccountDto editAccountDto = new EditAccountDto();
        editAccountDto.setAccountId(account.getId());
        editAccountDto.setUserId(account.getUser().getId());
        editAccountDto.setBalance(account.getBalance());
        editAccountDto.setCurrency(account.getCurrency());

        model.addAttribute("account", account);
        model.addAttribute("editAccountDto", editAccountDto);
        model.addAttribute("users", users);

        return "accounts/edit";
    }

    @PostMapping("/accounts/{id}/edit")
    public String updateAccount(@PathVariable Long id,
                                @ModelAttribute EditAccountDto editAccountDto,
                                RedirectAttributes redirectAttributes) {
        try {
            accountService.updateAccount(
                    editAccountDto.getAccountId(),
                    editAccountDto.getUserId(),
                    editAccountDto.getBalance(),
                    editAccountDto.getCurrency()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Счет успешно обновлен!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении: " + e.getMessage());
        }

        return "redirect:/admin/accounts";
    }

    @GetMapping("/categories")
    public String manageCategories(Model model) {
        model.addAttribute("categories", categoryService.getCategoriesHierarchy());
        model.addAttribute("topLevelCategories", categoryService.getAllTopLevelCategories());
        model.addAttribute("newCategory", new CategoryDto());
        return "admin/categories";
    }

    @PostMapping("/categories")
    public String createCategory(@ModelAttribute CategoryDto categoryDto) {
        categoryService.createCategory(
                categoryDto.getName(),
                categoryDto.getCategoryType(),
                categoryDto.getParentId()
        );
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/edit")
    public String updateCategory(@PathVariable Long id, @ModelAttribute CategoryDto categoryDto) {
        categoryService.updateCategory(
                id,
                categoryDto.getName(),
                categoryDto.getCategoryType(),
                categoryDto.getParentId()
        );
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/admin/categories";
    }
}