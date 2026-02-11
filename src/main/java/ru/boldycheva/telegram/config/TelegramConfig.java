package ru.boldycheva.telegram.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.boldycheva.telegram.bot.SimpleTestBot;

@Configuration
public class TelegramConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(SimpleTestBot bot) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(bot);
        System.out.println("✅ Telegram Bot зарегистрирован в API");
        return botsApi;
    }
}