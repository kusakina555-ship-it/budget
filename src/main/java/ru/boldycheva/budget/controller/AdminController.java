package ru.boldycheva.budget.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.boldycheva.budget.dto.AccountDto;
import ru.boldycheva.budget.dto.CategoryDto;
import ru.boldycheva.budget.entity.ExpenseType;
import ru.boldycheva.budget.service.AccountService;
import ru.boldycheva.budget.service.CategoryService;
import ru.boldycheva.budget.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/users")
    public String userManagement(Model model) {
        model.addAttribute("users", userService.getAllUsersWithoutPasswords());
        return "admin/users";
    }
    @GetMapping("/accounts")
    public String manageAccounts(Model model) {
        model.addAttribute("accounts", accountService.getAllAccounts());
        model.addAttribute("newAccount", new AccountDto());
        return "admin/accounts";
    }

    @GetMapping("/categories")
    public String manageCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("newCategory", new CategoryDto());
        model.addAttribute("expenseTypes", ExpenseType.values()); // Добавляем типы для формы
        return "admin/categories";
    }

    @PostMapping("/accounts")
    public String createAccount(@ModelAttribute AccountDto accountDto) {
        accountService.createAccount(accountDto.getInitialBalance(), accountDto.getCurrency());
        return "redirect:/admin/accounts";
    }

    @PostMapping("/categories")
    public String createCategory(@ModelAttribute CategoryDto categoryDto) {
        categoryService.createCategory(categoryDto.getName(), categoryDto.getExpenseType());
        return "redirect:/admin/categories";
    }
    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/edit")
    public String updateCategory(@PathVariable Long id, @ModelAttribute CategoryDto categoryDto) {
        categoryService.updateCategory(id, categoryDto.getName(), categoryDto.getExpenseType());
        return "redirect:/admin/categories";
    }
}