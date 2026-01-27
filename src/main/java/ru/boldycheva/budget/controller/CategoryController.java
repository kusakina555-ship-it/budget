package ru.boldycheva.budget.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.boldycheva.budget.dto.CategoryDto;
import ru.boldycheva.budget.service.CategoryFormService;
import ru.boldycheva.budget.service.CategoryService;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryFormService categoryFormService;

    @Autowired
    private CategoryService categoryService;

    // === HTML СТРАНИЦЫ ===

    @GetMapping("/new")
    public String showCreateCategoryForm(
            @RequestParam(value = "type", required = false) String categoryType,
            @RequestParam(value = "returnUrl", required = false, defaultValue = "/categories") String returnUrl,
            Model model,
            Authentication authentication) {

        String username = authentication.getName();
        CategoryFormService.CategoryFormData formData = categoryFormService.getCategoryFormData(username);

        // Создаем DTO с предустановленным типом
        CategoryDto categoryDto = new CategoryDto();
        if (categoryType != null) {
            categoryDto.setCategoryType(categoryType);
        }

        model.addAttribute("categoryDto", categoryDto);
        model.addAttribute("topLevelCategories", formData.getTopLevelCategories());
        model.addAttribute("incomeCategories", formData.getIncomeCategories());
        model.addAttribute("expenseCategories", formData.getExpenseCategories());
        model.addAttribute("isAdmin", formData.isAdmin());
        model.addAttribute("returnUrl", returnUrl);

        return "categories/new";
    }

    @PostMapping("/new")
    public String createCategory(
            @ModelAttribute CategoryDto categoryDto,
            @RequestParam(value = "returnUrl", required = false, defaultValue = "/categories") String returnUrl,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            String username = authentication.getName();
            categoryFormService.createCategory(categoryDto, username);
            redirectAttributes.addFlashAttribute("successMessage", "Категория успешно создана!");
            return "redirect:" + returnUrl;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при создании категории: " + e.getMessage());
            return "redirect:/categories/new?returnUrl=" + returnUrl;
        }
    }
}
