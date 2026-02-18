package ru.boldycheva.budget.controller;

import lombok.RequiredArgsConstructor;
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
public class TelegramAuthController {

    private final TelegramAuthService telegramAuthService;

    @PostMapping("/generate-code")
    public ResponseEntity<?> generateAuthCode(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        String authCode = telegramAuthService.generateAuthCode(userPrincipal.getId());

        Map<String, String> response = new HashMap<>();
        response.put("authCode", authCode);
        response.put("message", "Введите этот код в Telegram боте: " + authCode);
        response.put("expiresIn", "10 минут");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<?> getTelegramStatus(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        boolean isVerified = telegramAuthService.isChatVerified(userPrincipal.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("verified", isVerified);

        return ResponseEntity.ok(response);
    }
}