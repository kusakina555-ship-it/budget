package ru.boldycheva.mainapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")  // Добавляем профиль для тестов
class BudgetApplicationTests {

	@Test
	void contextLoads() {
		// Тест загружает контекст
	}
}
