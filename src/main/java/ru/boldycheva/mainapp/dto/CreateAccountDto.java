package ru.boldycheva.mainapp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public class CreateAccountDto {

    @NotNull(message = "Пользователь обязателен")
    private Long userId;

    @NotNull(message = "Начальный баланс обязателен")
    @DecimalMin(value = "0.00", message = "Баланс не может быть отрицательным")
    private BigDecimal initialBalance;

    @NotBlank(message = "Валюта обязательна")
    @Pattern(regexp = "^(RUB|USD)$", message = "Допустимые валюты: RUB, USD")
    private String currency;

    // Геттеры и сеттеры
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

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