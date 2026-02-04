package ru.boldycheva.budget.dto;

public class SubcategoryDto {
    private Long id;
    private String name;
    private String categoryType;

    // Конструкторы
    public SubcategoryDto() {}

    public SubcategoryDto(Long id, String name, String categoryType) {
        this.id = id;
        this.name = name;
        this.categoryType = categoryType;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }
}