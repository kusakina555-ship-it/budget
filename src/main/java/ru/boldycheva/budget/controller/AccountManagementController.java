package ru.boldycheva.budget.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.boldycheva.budget.service.AccountService;

@Controller
public class AccountManagementController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/accounts/{id}/delete")
    public String deleteAccount(@PathVariable Long id,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            // Вызываем новый метод с Authentication
            accountService.deleteAccount(id, authentication);
            redirectAttributes.addFlashAttribute("successMessage", "Счет успешно удален!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении: " + e.getMessage());
        }

        // Возвращаем на ту же страницу в зависимости от роли
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        return isAdmin ? "redirect:/admin/accounts" : "redirect:/accounts";
    }
}