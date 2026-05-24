package ru.boldycheva.budget.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.boldycheva.budget.service.TelegramAuthService;
import ru.boldycheva.budget.service.TelegramMessageService;

import java.util.Map;

@RestController
@RequestMapping("/api/bot")
@RequiredArgsConstructor
@Slf4j
public class BotController {

    private final TelegramAuthService telegramAuthService;
    private final TelegramMessageService telegramMessageService;

    @PostMapping("/message")
    public ResponseEntity<String> handleBotMessage(@RequestBody Map<String, Object> payload) {
        try {
            Long chatId = Long.valueOf(payload.get("chatId").toString());
            String text = payload.get("text").toString();
            String username = (String) payload.get("username");
            Long userId = Long.valueOf(payload.get("userId").toString());

            log.info("Received message from bot for user {} (chat {}): {}", userId, chatId, text);

            // Обрабатываем сообщение - метод ничего не возвращает
            telegramMessageService.processUserMessage(userId, chatId, text);

            // Возвращаем простое подтверждение
            return ResponseEntity.ok("Message processed successfully");

        } catch (Exception e) {
            log.error("Error processing bot message", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/generate-code/{userId}")
    public ResponseEntity<Map<String, String>> generateCode(@PathVariable Long userId) {
        try {
            String authCode = telegramAuthService.generateAuthCodeForBot(userId);

            return ResponseEntity.ok(Map.of(
                    "code", authCode,
                    "message", "Код авторизации сгенерирован"
            ));

        } catch (Exception e) {
            log.error("Error generating code for user {}", userId, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to generate code: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/user/{chatId}")
    public ResponseEntity<Map<String, Object>> getUserByChatId(@PathVariable Long chatId) {
        try {
            Long userId = telegramAuthService.getUserIdByChatId(chatId);
            boolean isVerified = telegramAuthService.isChatVerified(chatId);

            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "verified", isVerified
            ));

        } catch (Exception e) {
            log.error("Error getting user for chat {}", chatId, e);
            return ResponseEntity.notFound().build();
        }
    }
}