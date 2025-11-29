package ru.boldycheva.budget.dto;

import java.time.LocalDateTime;
import java.util.List;

public class UserDto {
    private Long id;
    private String userName;
    private String email;
    private LocalDateTime createdAt;
    private List<String> roles;

    public UserDto(Long id, String userName, String email, LocalDateTime createdAt, List<String> roles) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.createdAt = createdAt;
        this.roles = roles;
    }

    // геттеры
    public Long getId() { return id; }
    public String getUserName() { return userName; }
    public String getEmail() { return email; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<String> getRoles() { return roles; }
}
