package ru.boldycheva.mainapp.dto;

public class CreateSubcategoryDto {
    private String name;
    private String description;
    private String categoryType;
    private Long parentId;

    // Конструкторы
    public CreateSubcategoryDto() {}

    public CreateSubcategoryDto(String name, String description, String categoryType, Long parentId) {
        this.name = name;
        this.description = description;
        this.categoryType = categoryType;
        this.parentId = parentId;
    }

    // Геттеры и сеттеры
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
