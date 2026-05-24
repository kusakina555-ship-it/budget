package ru.boldycheva.budget.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.boldycheva.budget.security.UserPrincipal;
import ru.boldycheva.budget.service.TelegramAuthService;
import ru.boldycheva.budget.service.TelegramIntegrationService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
@Slf4j
public class TelegramAuthController {

    private final TelegramAuthService telegramAuthService;
    private final TelegramIntegrationService telegramIntegrationService;

    @PostMapping("/generate-code")
    public ResponseEntity<?> generateAuthCode(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Long userId = userPrincipal.getId();

            // Запрашиваем код у бота через API
            String authCode = telegramIntegrationService.requestAuthCodeFromBot(userId);

            Map<String, String> response = new HashMap<>();
            response.put("authCode", authCode);
            response.put("message", "Введите этот код в Telegram боте: " + authCode);
            response.put("expiresIn", "10 минут");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to generate auth code", e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Не удалось сгенерировать код");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getTelegramStatus(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Long userId = userPrincipal.getId();
            boolean isVerified = telegramAuthService.isChatVerified(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("verified", isVerified);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get telegram status", e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Не удалось получить статус");

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}