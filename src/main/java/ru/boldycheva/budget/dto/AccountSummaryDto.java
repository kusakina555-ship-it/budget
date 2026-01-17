package ru.boldycheva.budget.dto;

import java.math.BigDecimal;

public class AccountSummaryDto {
    private final BigDecimal totalBalance;
    private final int totalAccounts;
    private final long positiveBalanceAccounts;
    private final long zeroBalanceAccounts;
    private final long rubAccounts;
    private final long usdAccounts;
    private final BigDecimal rubTotal;
    private final BigDecimal usdTotal;

    public AccountSummaryDto(BigDecimal totalBalance, int totalAccounts,
                             long positiveBalanceAccounts, long zeroBalanceAccounts,
                             long rubAccounts, long usdAccounts,
                             BigDecimal rubTotal, BigDecimal usdTotal) {
        this.totalBalance = totalBalance;
        this.totalAccounts = totalAccounts;
        this.positiveBalanceAccounts = positiveBalanceAccounts;
        this.zeroBalanceAccounts = zeroBalanceAccounts;
        this.rubAccounts = rubAccounts;
        this.usdAccounts = usdAccounts;
        this.rubTotal = rubTotal;
        this.usdTotal = usdTotal;
    }

    // Геттеры
    public BigDecimal getTotalBalance() { return totalBalance; }
    public int getTotalAccounts() { return totalAccounts; }
    public long getPositiveBalanceAccounts() { return positiveBalanceAccounts; }
    public long getZeroBalanceAccounts() { return zeroBalanceAccounts; }
    public long getRubAccounts() { return rubAccounts; }
    public long getUsdAccounts() { return usdAccounts; }
    public BigDecimal getRubTotal() { return rubTotal; }
    public BigDecimal getUsdTotal() { return usdTotal; }
}