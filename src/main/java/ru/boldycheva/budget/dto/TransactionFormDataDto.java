package ru.boldycheva.budget.dto;

import ru.boldycheva.budget.entity.Account;
import ru.boldycheva.budget.entity.Category;
import ru.boldycheva.budget.entity.TransactionType;
import java.util.List;

public class TransactionFormDataDto {
    private List<Category> incomeCategories;
    private List<Category> expenseCategories;
    private TransactionType[] transactionTypes;
    private List<Account> accounts;
    private boolean isAdmin;

    // Конструкторы
    public TransactionFormDataDto() {}

    public TransactionFormDataDto(List<Category> incomeCategories,
                                  List<Category> expenseCategories,
                                  TransactionType[] transactionTypes,
                                  List<Account> accounts,
                                  boolean isAdmin) {
        this.incomeCategories = incomeCategories;
        this.expenseCategories = expenseCategories;
        this.transactionTypes = transactionTypes;
        this.accounts = accounts;
        this.isAdmin = isAdmin;
    }

    // Геттеры и сеттеры
    public List<Category> getIncomeCategories() { return incomeCategories; }
    public void setIncomeCategories(List<Category> incomeCategories) { this.incomeCategories = incomeCategories; }

    public List<Category> getExpenseCategories() { return expenseCategories; }
    public void setExpenseCategories(List<Category> expenseCategories) { this.expenseCategories = expenseCategories; }

    public TransactionType[] getTransactionTypes() { return transactionTypes; }
    public void setTransactionTypes(TransactionType[] transactionTypes) { this.transactionTypes = transactionTypes; }

    public List<Account> getAccounts() { return accounts; }
    public void setAccounts(List<Account> accounts) { this.accounts = accounts; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
}
