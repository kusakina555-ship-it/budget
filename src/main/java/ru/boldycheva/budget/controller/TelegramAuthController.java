package ru.boldycheva.budget.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.boldycheva.budget.security.UserPrincipal;
import ru.boldycheva.budget.service.TelegramAuthService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
@Slf4j
public class TelegramAuthController {

    private final TelegramAuthService telegramAuthService;

    @PostMapping("/generate-code")
    public ResponseEntity<?> generateAuthCode(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Long userId;

            // Пытаемся получить ID из аутентификации
            if (userPrincipal != null) {
                userId = userPrincipal.getId();
                log.info("Generating auth code for authenticated user ID: {}", userId);
            } else {
                // Если не получилось, используем тестовый режим
                // В реальном проекте здесь можно получить пользователя из cookie или сессии
                userId = 1L; // По умолчанию для User1
                log.warn("User not authenticated, using default user ID: {}", userId);
            }

            String authCode = telegramAuthService.generateAuthCode(userId);

            Map<String, String> response = new HashMap<>();
            response.put("authCode", authCode);
            response.put("message", "Введите этот код в Telegram боте: " + authCode);
            response.put("expiresIn", "10 минут");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to generate auth code: {}", e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Не удалось сгенерировать код");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getTelegramStatus(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Long userId;

            if (userPrincipal != null) {
                userId = userPrincipal.getId();
                log.info("Checking status for authenticated user ID: {}", userId);
            } else {
                userId = 1L;
                log.warn("User not authenticated, checking status for default user ID: {}", userId);
            }

            boolean isVerified = telegramAuthService.isChatVerified(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("verified", isVerified);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get telegram status: {}", e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Не удалось получить статус");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}