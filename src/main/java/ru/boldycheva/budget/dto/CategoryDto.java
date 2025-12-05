package ru.boldycheva.budget.dto;

import ru.boldycheva.budget.entity.ExpenseType;

public class CategoryDto {
    private String name;
    private ExpenseType expenseType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExpenseType getExpenseType() {
        return expenseType;
    }

    public void setExpenseType(ExpenseType expenseType) {
        this.expenseType = expenseType;
    }
}
