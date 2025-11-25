package ru.boldycheva.budget.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 500)
    private String comment;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 10)
    private TransactionType transactionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id")
    private Subcategory subcategory;

    @OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY)
    private List<Account> accounts = new ArrayList<>();

    // Конструкторы
    public Transaction() {}

    public Transaction(BigDecimal amount, String comment, LocalDateTime transactionDate,
                       TransactionType transactionType, Category category) {
        this.amount = amount;
        this.comment = comment;
        this.transactionDate = transactionDate;
        this.transactionType = transactionType;
        this.category = category;
    }

    public Transaction(BigDecimal amount, String comment, LocalDateTime transactionDate,
                       TransactionType transactionType, Category category, Subcategory subcategory) {
        this.amount = amount;
        this.comment = comment;
        this.transactionDate = transactionDate;
        this.transactionType = transactionType;
        this.category = category;
        this.subcategory = subcategory;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Subcategory getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(Subcategory subcategory) {
        this.subcategory = subcategory;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", amount=" + amount +
                ", comment='" + comment + '\'' +
                ", transactionDate=" + transactionDate +
                ", transactionType=" + transactionType +
                ", category=" + (category != null ? category.getName() : "null") +
                ", subcategory=" + (subcategory != null ? subcategory.getName() : "null") +
                '}';
    }

enum TransactionType {
    INCOME,    // Плюс (доход)
    EXPENSE    // Минус (расход)
}
}