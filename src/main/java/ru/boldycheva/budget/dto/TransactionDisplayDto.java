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
    private String accountName;
    private String counterpartyAccountName; // Для переводов - кому перевели
    private Long relatedTransactionId; // Для переводов - ID связанной транзакции
    private String currency;
    private String formattedDisplay; // Форматированное отображение

    // Конструкторы
    public TransactionDisplayDto() {}

    public TransactionDisplayDto(Long id, BigDecimal amount, String comment,
                                 LocalDateTime transactionDate, TransactionType transactionType,
                                 String categoryName, String accountName,
                                 String counterpartyAccountName, Long relatedTransactionId,
                                 String currency) {
        this.id = id;
        this.amount = amount;
        this.comment = comment;
        this.transactionDate = transactionDate;
        this.transactionType = transactionType;
        this.categoryName = categoryName;
        this.accountName = accountName;
        this.counterpartyAccountName = counterpartyAccountName;
        this.relatedTransactionId = relatedTransactionId;
        this.currency = currency;
        this.formattedDisplay = formatDisplay();
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

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getCounterpartyAccountName() { return counterpartyAccountName; }
    public void setCounterpartyAccountName(String counterpartyAccountName) { this.counterpartyAccountName = counterpartyAccountName; }

    public Long getRelatedTransactionId() { return relatedTransactionId; }
    public void setRelatedTransactionId(Long relatedTransactionId) { this.relatedTransactionId = relatedTransactionId; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getFormattedDisplay() {
        if (formattedDisplay == null) {
            formattedDisplay = formatDisplay();
        }
        return formattedDisplay;
    }

    // Метод для форматирования отображения
    private String formatDisplay() {
        StringBuilder sb = new StringBuilder();

        if (transactionType == TransactionType.TRANSFER) {
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                // Перевод отправителя (минус)
                sb.append("➡️ Перевод со счета: ").append(accountName)
                        .append(" на счет: ").append(counterpartyAccountName)
                        .append(" -").append(amount.abs()).append(" ").append(currency);
            } else {
                // Перевод получателя (плюс)
                sb.append("⬅️ Перевод на счет: ").append(accountName)
                        .append(" от счета: ").append(counterpartyAccountName)
                        .append(" +").append(amount).append(" ").append(currency);
            }
        } else if (transactionType == TransactionType.INCOME) {
            sb.append("💰 Доход: ").append(categoryName)
                    .append(" (счет: ").append(accountName).append(")")
                    .append(" +").append(amount).append(" ").append(currency);
        } else if (transactionType == TransactionType.EXPENSE) {
            sb.append("💸 Расход: ").append(categoryName)
                    .append(" (счет: ").append(accountName).append(")")
                    .append(" -").append(amount).append(" ").append(currency);
        }

        if (comment != null && !comment.isEmpty()) {
            sb.append(" | ").append(comment);
        }

        return sb.toString();
    }
}
