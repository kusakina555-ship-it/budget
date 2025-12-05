package ru.boldycheva.budget.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.boldycheva.budget.dto.CategoryDto;
import ru.boldycheva.budget.entity.Category;
import ru.boldycheva.budget.entity.ExpenseType;
import ru.boldycheva.budget.repository.CategoryRepository;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // Получить все категории
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Получить категории по типу (доход/расход)
    public List<Category> getCategoriesByType(ExpenseType expenseType) {
        return categoryRepository.findByExpenseType(expenseType);
    }

    // Создать категорию
    @Transactional
    public Category createCategory(String name, ExpenseType expenseType) {
        // Проверяем, существует ли категория с таким именем
        if (categoryRepository.existsByName(name)) {
            throw new RuntimeException("Категория с названием '" + name + "' уже существует");
        }

        Category category = new Category();
        category.setName(name);
        category.setExpenseType(expenseType);

        return categoryRepository.save(category);
    }

    // Создать категорию из DTO
    @Transactional
    public Category createCategory(CategoryDto categoryDto) {
        return createCategory(categoryDto.getName(), categoryDto.getExpenseType());
    }

    // Обновить категорию
    @Transactional
    public Category updateCategory(Long id, String name, ExpenseType expenseType) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));

        // Проверяем, не используется ли это имя другой категорией
        if (!category.getName().equals(name) && categoryRepository.existsByName(name)) {
            throw new RuntimeException("Категория с названием '" + name + "' уже существует");
        }

        category.setName(name);
        category.setExpenseType(expenseType);

        return categoryRepository.save(category);
    }

    // Удалить категорию
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Категория не найдена");
        }
        categoryRepository.deleteById(id);
    }

    // Получить категорию по ID
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));
    }

    // Получить категорию по имени
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Категория '" + name + "' не найдена"));
    }
}
