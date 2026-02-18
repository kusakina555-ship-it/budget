package ru.boldycheva.budget.service;

import lombok.RequiredArgsConstructor;
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
public class TelegramAuthService {

    private final TelegramUserRepository telegramUserRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public String generateAuthCode(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

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

        // Также генерируем JWT токен с кодом
        String jwtToken = jwtTokenProvider.generateTelegramAuthToken(user.getUserName(), userId);

        return authCode; // Или можно вернуть JWT токен
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
