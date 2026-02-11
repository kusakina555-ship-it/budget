package ru.boldycheva.mainapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.boldycheva.mainapp.dto.CategoryDto;
import ru.boldycheva.mainapp.entity.Category;
import ru.boldycheva.mainapp.repository.CategoryRepository;

import java.util.List;

@Service
public class CategoryManagementService {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    // Создать категорию с проверкой прав
    @Transactional
    public Category createCategory(CategoryDto categoryDto, Authentication authentication) {
        return categoryService.createCategory(categoryDto, authentication);
    }

    // Получить данные для формы создания категории
    public CategoryFormData getCategoryFormData(Authentication authentication) {
        boolean isAdmin = isAdmin(authentication);

        List<Category> topLevelCategories = categoryService.getAllTopLevelCategories();
        List<Category> incomeCategories = categoryService.getTopLevelCategoriesByType("INCOME");
        List<Category> expenseCategories = categoryService.getTopLevelCategoriesByType("EXPENSE");

        return new CategoryFormData(topLevelCategories, incomeCategories, expenseCategories, isAdmin);
    }

    // DTO для данных формы категории
    public static class CategoryFormData {
        private List<Category> topLevelCategories;
        private List<Category> incomeCategories;
        private List<Category> expenseCategories;
        private boolean isAdmin;

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

    // Проверка, является ли пользователь админом
    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}