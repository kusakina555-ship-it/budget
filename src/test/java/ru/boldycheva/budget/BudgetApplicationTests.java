package ru.boldycheva.budget;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"spring.liquibase.enabled=false",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
class BudgetApplicationTests {

	@Test
	void contextLoads() {
		// Тест проверяет что контекст Spring загружается
	}
}
