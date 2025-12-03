package ru.boldycheva.budget.dto;

import java.math.BigDecimal;

public class AccountDto {

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
        this.currency = currency;
    }

    private BigDecimal initialBalance;
    private String currency;
}
