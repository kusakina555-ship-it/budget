package ru.boldycheva.telegram.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class SimpleTestBot extends TelegramLongPollingBot {

    private final String botUsername;

    public SimpleTestBot(@Value("${telegram.bot.token}") String botToken,
                         @Value("${telegram.bot.username}") String botUsername) {
        super(botToken);
        this.botUsername = botUsername;
        System.out.println("🤖 Бот инициализирован: " + botUsername);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            System.out.println("📨 Получено сообщение: " + messageText + " от chatId: " + chatId);

            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText("Привет! Я тестовый бот. Вы написали: " + messageText);

            try {
                execute(message);
                System.out.println("✅ Ответ отправлен");
            } catch (TelegramApiException e) {
                System.err.println("❌ Ошибка отправки: " + e.getMessage());
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
}
