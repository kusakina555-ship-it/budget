package ru.boldycheva.budget.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "telegram_users")
@Data
public class TelegramUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "chat_id", unique = true)
    private Long chatId;

    @Column(name = "telegram_username")
    private String telegramUsername;

    @Column(name = "auth_code")
    private String authCode;

    @Column(name = "auth_code_expiry")
    private LocalDateTime authCodeExpiry;

    @Column(name = "is_verified")
    private boolean verified = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
}
