package ru.boldycheva.budget.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.boldycheva.budget.dto.CategoryDto;
import ru.boldycheva.budget.entity.Category;
import ru.boldycheva.budget.entity.User;
import ru.boldycheva.budget.repository.CategoryRepository;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // Получить все категории верхнего уровня
    public List<Category> getAllTopLevelCategories() {
        return categoryRepository.findByParentIsNull();
    }

    // Получить все категории (включая подкатегории)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Получить подкатегории для родительской категории
    public List<Category> getSubcategories(Long parentId) {
        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Родительская категория не найдена"));
        return parent.getSubcategories();
    }

    // Создать категорию (верхнего уровня или подкатегорию)
    @Transactional
    public Category createCategory(String name, String categoryType, Long parentId, Authentication authentication) {
        if (categoryRepository.existsByName(name)) {
            throw new RuntimeException("Категория с названием '" + name + "' уже существует");
        }

        // Проверяем права
        boolean isAdmin = isAdmin(authentication);

        // Если создаем категорию верхнего уровня - проверяем права
        if (parentId == null && !isAdmin) {
            throw new RuntimeException("Только администратор может создавать категории верхнего уровня");
        }

        // Если создаем подкатегорию - получаем родительскую категорию
        Category parent = null;
        if (parentId != null) {
            parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Родительская категория не найдена"));

            // Проверяем, что тип подкатегории совпадает с типом родительской
            if (!categoryType.equals(parent.getCategoryType())) {
                throw new RuntimeException("Тип подкатегории должен совпадать с типом родительской категории");
            }
        }

        Category category = new Category();
        category.setName(name);
        category.setCategoryType(categoryType);
        category.setParent(parent);

        return categoryRepository.save(category);
    }

    // Создать из DTO
    @Transactional
    public Category createCategory(CategoryDto categoryDto, Authentication authentication) {
        return createCategory(
                categoryDto.getName(),
                categoryDto.getCategoryType(),
                categoryDto.getParentId(),
                authentication
        );
    }

    // Обновить категорию
    @Transactional
    public Category updateCategory(Long id, String name, String categoryType, Long parentId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));

        if (!category.getName().equals(name) && categoryRepository.existsByName(name)) {
            throw new RuntimeException("Категория с названием '" + name + "' уже существует");
        }

        category.setName(name);
        category.setCategoryType(categoryType);

        if (parentId != null) {
            Category parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Родительская категория не найдена"));
            // Проверяем, чтобы не создавать циклических ссылок
            if (isCircularReference(category, parent)) {
                throw new RuntimeException("Нельзя назначить родителем саму себя или дочернюю категорию");
            }
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        return categoryRepository.save(category);
    }

    // Удалить категорию
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));

        // Проверяем, есть ли у категории подкатегории или транзакции
        if (!category.getSubcategories().isEmpty()) {
            throw new RuntimeException("Нельзя удалить категорию, у которой есть подкатегории");
        }

        if (!category.getTransactions().isEmpty()) {
            throw new RuntimeException("Нельзя удалить категорию, которая используется в транзакциях");
        }

        categoryRepository.delete(category);
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

    // Получить категории по типу
    public List<Category> getCategoriesByType(String categoryType) {
        return categoryRepository.findByCategoryType(categoryType);
    }

    // Получить все категории верхнего уровня по типу
    public List<Category> getTopLevelCategoriesByType(String categoryType) {
        return categoryRepository.findByCategoryTypeAndParentIsNull(categoryType);
    }

    // Получить все категории в иерархическом виде
    public List<Category> getCategoriesHierarchy() {
        List<Category> topLevel = getAllTopLevelCategories();
        return topLevel;
    }

    // Получить подкатегории для формы создания транзакции
    public List<Category> getAvailableCategoriesForTransaction(Authentication authentication) {
        boolean isAdmin = isAdmin(authentication);
        if (isAdmin) {
            return categoryRepository.findAll();
        } else {
            // Возвращаем все категории (включая подкатегории)
            return categoryRepository.findAll();
        }
    }

    // Проверка циклических ссылок
    private boolean isCircularReference(Category category, Category potentialParent) {
        if (category.getId() != null && category.getId().equals(potentialParent.getId())) {
            return true;
        }

        Category currentParent = potentialParent.getParent();
        while (currentParent != null) {
            if (category.getId() != null && category.getId().equals(currentParent.getId())) {
                return true;
            }
            currentParent = currentParent.getParent();
        }

        return false;
    }

    // Проверка, является ли пользователь админом
    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    public Category createCategory(String name, String categoryType, Long parentId, User user) {
        // Перенесите логику из метода с Authentication
        boolean isAdmin = user.getRoles().contains("ADMIN");

        if (categoryRepository.existsByName(name)) {
            throw new RuntimeException("Категория с названием '" + name + "' уже существует");
        }

        // Если создаем категорию верхнего уровня - проверяем права
        if (parentId == null && !isAdmin) {
            throw new SecurityException("Только администратор может создавать категории верхнего уровня");
        }

        Category parent = null;
        if (parentId != null) {
            parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Родительская категория не найдена"));

            if (!categoryType.equals(parent.getCategoryType())) {
                throw new RuntimeException("Тип подкатегории должен совпадать с типом родительской категории");
            }
        }

        Category category = new Category();
        category.setName(name);
        category.setCategoryType(categoryType);
        category.setParent(parent);

        return categoryRepository.save(category);
    }

    public boolean categoryExists(Long categoryId) {
        if (categoryId == null) {
            return false;
        }
        return categoryRepository.existsById(categoryId);
    }
}
