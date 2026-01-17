package ru.boldycheva.budget.dto;

public class AccountDataDto {
    private final Long userId;
    private final String username;
    private final boolean isAdmin;

    public AccountDataDto(Long userId, String username, boolean isAdmin) {
        this.userId = userId;
        this.username = username;
        this.isAdmin = isAdmin;
    }

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public boolean isAdmin() { return isAdmin; }
}