package ru.boldycheva.budget.dto;

import java.util.List;

public class CategoryWithSubcategoriesDto {
    private Long id;
    private String name;
    private String categoryType;
    private List<CategoryDto> subcategories;

    // Конструкторы, геттеры, сеттеры
    public CategoryWithSubcategoriesDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategoryType() { return categoryType; }
    public void setCategoryType(String categoryType) { this.categoryType = categoryType; }

    public List<CategoryDto> getSubcategories() { return subcategories; }
    public void setSubcategories(List<CategoryDto> subcategories) { this.subcategories = subcategories; }
}