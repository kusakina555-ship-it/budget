package ru.boldycheva.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public class EditAccountDto {

    @NotNull(message = "ID счета обязателен")
    private Long accountId;

    @NotNull(message = "Пользователь обязателен")
    private Long userId;

    @NotNull(message = "Баланс обязателен")
    @DecimalMin(value = "0.00", message = "Баланс не может быть отрицательным")
    private BigDecimal balance;

    @NotNull(message = "Валюта обязательна")
    @Pattern(regexp = "^(RUB|USD)$", message = "Допустимые валюты: RUB, USD")
    private String currency;

    // Геттеры и сеттеры
    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        if (currency != null) {
            currency = currency.toUpperCase();
            if (!currency.equals("RUB") && !currency.equals("USD")) {
                currency = "RUB";
            }
        } else {
            currency = "RUB";
        }
        this.currency = currency;
    }
}
