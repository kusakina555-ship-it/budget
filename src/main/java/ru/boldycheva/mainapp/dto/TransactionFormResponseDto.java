package ru.boldycheva.mainapp.dto;

import ru.boldycheva.mainapp.entity.Account;
import ru.boldycheva.mainapp.entity.Category;

import java.util.List;

public class TransactionFormResponseDto {
    private List<Category> incomeCategories;
    private List<Category> expenseCategories;
    private List<Account> accounts;
    private boolean isAdmin;

    // Конструкторы
    public TransactionFormResponseDto() {}

    public TransactionFormResponseDto(List<Category> incomeCategories,
                                      List<Category> expenseCategories,
                                      List<Account> accounts,
                                      boolean isAdmin) {
        this.incomeCategories = incomeCategories;
        this.expenseCategories = expenseCategories;
        this.accounts = accounts;
        this.isAdmin = isAdmin;
    }

    // Геттеры и сеттеры
    public List<Category> getIncomeCategories() { return incomeCategories; }
    public void setIncomeCategories(List<Category> incomeCategories) { this.incomeCategories = incomeCategories; }

    public List<Category> getExpenseCategories() { return expenseCategories; }
    public void setExpenseCategories(List<Category> expenseCategories) { this.expenseCategories = expenseCategories; }

    public List<Account> getAccounts() { return accounts; }
    public void setAccounts(List<Account> accounts) { this.accounts = accounts; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
}
