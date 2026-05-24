package ru.boldycheva.budget.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.boldycheva.budget.entity.TelegramUser;
import java.util.Optional;

@Repository
public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {
    Optional<TelegramUser> findByUserId(Long userId);
    Optional<TelegramUser> findByChatId(Long chatId);
    Optional<TelegramUser> findByAuthCode(String authCode);
    boolean existsByChatId(Long chatId);
}
