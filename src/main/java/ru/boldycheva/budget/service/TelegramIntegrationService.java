package ru.boldycheva.budget.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramIntegrationService {

    @Value("${bot.api.url:http://telegram-bot:8081}")
    private String botApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String requestAuthCodeFromBot(Long userId) {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    botApiUrl + "/api/bot/generate-code/" + userId,
                    null,
                    Map.class
            );

            if (response.getBody() != null && response.getBody().containsKey("code")) {
                return (String) response.getBody().get("code");
            } else {
                throw new RuntimeException("Invalid response from bot");
            }

        } catch (Exception e) {
            log.error("Failed to request auth code from bot for user {}", userId, e);
            throw new RuntimeException("Failed to communicate with bot service", e);
        }
    }
}