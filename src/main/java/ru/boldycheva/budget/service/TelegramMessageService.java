package ru.boldycheva.budget.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TelegramMessageService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public TelegramMessageService(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    public void processUserMessage(Long userId, Long chatId, String message) {
        log.info("Processing message from user {}: {}", userId, message);
        // Здесь будет логика обработки сообщений от авторизованных пользователей
    }

    public void sendMessageToChat(Long chatId, String text) {
        try {
            String payload = String.format("{\"chatId\":\"%d\",\"text\":\"%s\"}", chatId, text);
            messagingTemplate.convertAndSend("/topic/telegram-responses", payload);
            log.info("Message sent to Telegram chat {}: {}", chatId, text);
        } catch (Exception e) {
            log.error("Error sending message to Telegram", e);
        }
    }
}