package ru.boldycheva.budget.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.boldycheva.budget.dto.EditAccountDto;
import ru.boldycheva.budget.entity.User;
import ru.boldycheva.budget.repository.UserRepository;
import ru.boldycheva.budget.service.AccountService;

import java.math.BigDecimal;

@Controller
@RequestMapping("/my-accounts")
public class UserAccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository;

    // === СОЗДАНИЕ СЧЕТА ===

    // Показать форму создания счета
    @GetMapping("/new")
    public String showCreateAccountForm(Model model) {
        model.addAttribute("pageTitle", "Создать новый счет");
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
            // Получаем текущего пользователя
            String username = authentication.getName();
            User currentUser = userRepository.findByUserName(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // Проверяем права доступа
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            // Получаем счет с проверкой прав
            var account = accountService.getAccountWithPermissionCheck(id, currentUser.getId(), isAdmin);

            // Создаем DTO для формы
            EditAccountDto editAccountDto = new EditAccountDto();
            editAccountDto.setAccountId(account.getId());
            editAccountDto.setUserId(account.getUser().getId());
            editAccountDto.setBalance(account.getBalance());
            editAccountDto.setCurrency(account.getCurrency());

            model.addAttribute("account", account);
            model.addAttribute("editAccountDto", editAccountDto);
            model.addAttribute("isAdmin", isAdmin);

            return "accounts/user-edit";

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/accounts";
        }
    }

    // Обновить счет
    @PostMapping("/{id}/edit")
    public String updateAccount(@PathVariable Long id,
                                @ModelAttribute EditAccountDto editAccountDto,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            // Получаем текущего пользователя
            String username = authentication.getName();
            User currentUser = userRepository.findByUserName(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // Проверяем права доступа
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            // Для обычных пользователей проверяем, что они не меняют владельца
            if (!isAdmin && !editAccountDto.getUserId().equals(currentUser.getId())) {
                throw new RuntimeException("Обычные пользователи не могут менять владельца счета");
            }

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

        return "redirect:/accounts";
    }

    // === УДАЛЕНИЕ СЧЕТА ===

    // Удалить счет (теперь используется AccountManagementController)
}