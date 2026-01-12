package ru.boldycheva.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public class AccountDto {

    @NotNull(message = "Начальный баланс обязателен")
    @DecimalMin(value = "0.00", message = "Баланс не может быть отрицательным")
    private BigDecimal initialBalance;

    @NotBlank(message = "Валюта обязательна")
    @Pattern(regexp = "^(RUB|USD)$", message = "Допустимые валюты: RUB, USD")
    private String currency;

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        // Приводим к верхнему регистру
        if (currency != null) {
            currency = currency.toUpperCase();
            // Оставляем только RUB или USD
            if (!currency.equals("RUB") && !currency.equals("USD")) {
                // По умолчанию RUB
                currency = "RUB";
            }
        } else {
            currency = "RUB";
        }
        this.currency = currency;
    }
}
