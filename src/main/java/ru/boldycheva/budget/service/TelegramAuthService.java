package ru.boldycheva.budget.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.boldycheva.budget.entity.TelegramUser;
import ru.boldycheva.budget.entity.User;
import ru.boldycheva.budget.repository.TelegramUserRepository;
import ru.boldycheva.budget.repository.UserRepository;
import ru.boldycheva.budget.security.jwt.JwtTokenProvider;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramAuthService {

    private final TelegramUserRepository telegramUserRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public String generateAuthCode(Long userId) {
        try {
            log.info("Generating auth code for user ID: {}", userId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            String authCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(10);

            TelegramUser telegramUser = telegramUserRepository.findByUserId(userId)
                    .orElse(new TelegramUser());

            telegramUser.setUserId(userId);
            telegramUser.setAuthCode(authCode);
            telegramUser.setAuthCodeExpiry(expiryTime);
            telegramUser.setVerified(false);
            telegramUser.setCreatedAt(LocalDateTime.now());

            telegramUserRepository.save(telegramUser);

            log.info("Auth code generated successfully for user {}: {}", userId, authCode);
            return authCode;

        } catch (Exception e) {
            log.error("Error generating auth code for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to generate auth code: " + e.getMessage());
        }
    }

    @Transactional
    public boolean verifyAuthCode(String authCode, Long chatId, String telegramUsername) {
        TelegramUser telegramUser = telegramUserRepository.findByAuthCode(authCode)
                .orElseThrow(() -> new RuntimeException("Invalid auth code"));

        if (telegramUser.isVerified()) {
            return false;
        }

        if (telegramUser.getAuthCodeExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }

        telegramUser.setChatId(chatId);
        telegramUser.setTelegramUsername(telegramUsername);
        telegramUser.setVerified(true);
        telegramUser.setVerifiedAt(LocalDateTime.now());

        telegramUserRepository.save(telegramUser);

        return true;
    }

    public boolean isChatVerified(Long chatId) {
        return telegramUserRepository.findByChatId(chatId)
                .map(TelegramUser::isVerified)
                .orElse(false);
    }

    public Long getUserIdByChatId(Long chatId) {
        return telegramUserRepository.findByChatId(chatId)
                .map(TelegramUser::getUserId)
                .orElse(null);
    }
}
