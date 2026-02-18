package ru.boldycheva.budget.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramMessageService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

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