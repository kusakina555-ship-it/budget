package ru.boldycheva.budget.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import ru.boldycheva.budget.service.TelegramAuthService;
import ru.boldycheva.budget.service.TelegramMessageService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TelegramWebSocketController {

    private final ObjectMapper objectMapper;
    private final TelegramAuthService telegramAuthService;
    private final TelegramMessageService telegramMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/telegram-messages")
    public void handleTelegramMessage(String messagePayload) {
        try {
            JsonNode message = objectMapper.readTree(messagePayload);
            Long chatId = message.get("chatId").asLong();
            String text = message.get("text").asText();

            log.info("Received message from Telegram chat {}: {}", chatId, text);

            // Проверяем, авторизован ли чат
            if (!telegramAuthService.isChatVerified(chatId)) {
                // Если это не код авторизации
                if (!text.startsWith("/auth ")) {
                    sendTelegramMessage(chatId, "❌ Авторизуйтесь с помощью приложения. Введите код авторизации в формате: /auth ВАШ_КОД");
                    return;
                }

                // Обрабатываем код авторизации
                String authCode = text.substring(6).trim();
                try {
                    boolean verified = telegramAuthService.verifyAuthCode(authCode, chatId, null);
                    if (verified) {
                        sendTelegramMessage(chatId, "✅ Авторизация успешна! Теперь вы можете пользоваться ботом.");
                    } else {
                        sendTelegramMessage(chatId, "❌ Неверный или просроченный код авторизации.");
                    }
                } catch (Exception e) {
                    sendTelegramMessage(chatId, "❌ Ошибка авторизации: " + e.getMessage());
                }
                return;
            }

            // Для авторизованных пользователей обрабатываем сообщения
            Long userId = telegramAuthService.getUserIdByChatId(chatId);
            // Здесь обрабатываем обычные сообщения от авторизованных пользователей
            telegramMessageService.processUserMessage(userId, chatId, text);

        } catch (Exception e) {
            log.error("Error handling telegram message", e);
        }
    }

    private void sendTelegramMessage(Long chatId, String text) {
        try {
            String response = String.format("{\"chatId\":\"%d\",\"text\":\"%s\"}", chatId, text);
            messagingTemplate.convertAndSend("/topic/telegram-responses", response);
        } catch (Exception e) {
            log.error("Error sending message to Telegram", e);
        }
    }
}
