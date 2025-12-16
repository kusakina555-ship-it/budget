package ru.boldycheva.budget.dto;

public class CategoryDto {
    private String name;
    private String categoryType; // "INCOME" или "EXPENSE"
    private Long parentId; // может быть null для категорий верхнего уровня

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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
