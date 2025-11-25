package ru.boldycheva.budget.entity;

public enum ExpenseType {
    TRANSPORT("Транспорт"),
    PETS("Домашние животные"),
    FOOD("Еда"),
    HOUSEHOLD("Дом"),
    ENTERTAINMENT("Развлечения"),
    HEALTH("Здоровье"),
    INCOME("Доходы");

    private final String displayName;

    ExpenseType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;  //  Геттер для получения русского названия
    }
}