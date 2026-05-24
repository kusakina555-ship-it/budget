package ru.boldycheva.budget.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.boldycheva.budget.entity.TelegramUser;
import ru.boldycheva.budget.entity.User;
import ru.boldycheva.budget.repository.TelegramUserRepository;
import ru.boldycheva.budget.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramAuthService {

    private final TelegramUserRepository telegramUserRepository;
    private final UserRepository userRepository;

    @Transactional
    public String generateAuthCodeForBot(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Возвращаем userId, который бот использует для генерации кода
        // Бот сам сгенерирует и сохранит код в своей БД
        return userId.toString();
    }

    @Transactional
    public void confirmAuth(Long userId, Long chatId, String telegramUsername) {
        TelegramUser telegramUser = telegramUserRepository.findByUserId(userId)
                .orElse(new TelegramUser());

        telegramUser.setUserId(userId);
        telegramUser.setChatId(chatId);
        telegramUser.setTelegramUsername(telegramUsername);
        telegramUser.setVerified(true);
        telegramUser.setVerifiedAt(LocalDateTime.now());

        telegramUserRepository.save(telegramUser);

        log.info("User {} confirmed with chat {}", userId, chatId);
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