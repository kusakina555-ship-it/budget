package ru.boldycheva.budget.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.boldycheva.budget.dto.CategoryDto;
import ru.boldycheva.budget.entity.Category;
import ru.boldycheva.budget.entity.User;

import java.util.List;

@Service
public class CategoryFormService {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    public CategoryFormData getCategoryFormData(String username) {
        User user = userService.getUserByUsername(username);
        boolean isAdmin = user.getRoles().contains("ADMIN");

        List<Category> topLevelCategories = categoryService.getAllTopLevelCategories();
        List<Category> incomeCategories = categoryService.getTopLevelCategoriesByType("INCOME");
        List<Category> expenseCategories = categoryService.getTopLevelCategoriesByType("EXPENSE");

        return new CategoryFormData(topLevelCategories, incomeCategories, expenseCategories, isAdmin);
    }

    public Category createCategory(CategoryDto categoryDto, String username) {
        User user = userService.getUserByUsername(username);
        return categoryService.createCategory(
                categoryDto.getName(),
                categoryDto.getCategoryType(),
                categoryDto.getParentId(),
                user
        );
    }

    // Внутренний DTO класса
    public static class CategoryFormData {
        private final List<Category> topLevelCategories;
        private final List<Category> incomeCategories;
        private final List<Category> expenseCategories;
        private final boolean isAdmin;

        public CategoryFormData(List<Category> topLevelCategories,
                                List<Category> incomeCategories,
                                List<Category> expenseCategories,
                                boolean isAdmin) {
            this.topLevelCategories = topLevelCategories;
            this.incomeCategories = incomeCategories;
            this.expenseCategories = expenseCategories;
            this.isAdmin = isAdmin;
        }

        public List<Category> getTopLevelCategories() { return topLevelCategories; }
        public List<Category> getIncomeCategories() { return incomeCategories; }
        public List<Category> getExpenseCategories() { return expenseCategories; }
        public boolean isAdmin() { return isAdmin; }
    }
}
