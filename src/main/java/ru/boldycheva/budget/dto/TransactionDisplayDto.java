package ru.boldycheva.budget.dto;

import ru.boldycheva.budget.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDisplayDto {
    private Long id;
    private BigDecimal amount;
    private String comment;
    private LocalDateTime transactionDate;
    private TransactionType transactionType;
    private String categoryName;
    private String currency;

    // Конструкторы
    public TransactionDisplayDto() {}

    public TransactionDisplayDto(Long id, BigDecimal amount, String comment,
                                 LocalDateTime transactionDate, TransactionType transactionType,
                                 String categoryName, String currency) {
        this.id = id;
        this.amount = amount;
        this.comment = comment;
        this.transactionDate = transactionDate;
        this.transactionType = transactionType;
        this.categoryName = categoryName;
        this.currency = currency;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
