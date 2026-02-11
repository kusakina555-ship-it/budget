package ru.boldycheva.mainapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

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
    @Column(name = "transaction_type", nullable = false, length = 15)
    private TransactionType transactionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    // Для переводов - ID связанной транзакции
    @Column(name = "related_transaction_id")
    private Long relatedTransactionId;

    // Для переводов - счет контрагента (для отображения)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counterparty_account_id")
    private Account counterpartyAccount;

    // Конструкторы
    public Transaction() {}

    public Transaction(BigDecimal amount, String comment, LocalDateTime transactionDate,
                       TransactionType transactionType, Category category, Account account) {
        this.amount = amount;
        this.comment = comment;
        this.transactionDate = transactionDate;
        this.transactionType = transactionType;
        this.category = category;
        this.account = account;
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

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public Long getRelatedTransactionId() { return relatedTransactionId; }
    public void setRelatedTransactionId(Long relatedTransactionId) { this.relatedTransactionId = relatedTransactionId; }

    public Account getCounterpartyAccount() { return counterpartyAccount; }
    public void setCounterpartyAccount(Account counterpartyAccount) { this.counterpartyAccount = counterpartyAccount; }

    // Вспомогательные методы
    public boolean isTransfer() {
        return TransactionType.TRANSFER == transactionType;
    }

    public boolean hasCounterparty() {
        return counterpartyAccount != null;
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
                ", account=" + (account != null ? "Account#" + account.getId() : "null") +
                ", counterpartyAccount=" + (counterpartyAccount != null ? "Account#" + counterpartyAccount.getId() : "null") +
                '}';
    }
}