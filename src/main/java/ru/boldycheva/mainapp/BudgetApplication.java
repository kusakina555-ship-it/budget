package ru.boldycheva.mainapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BudgetApplication {
	public static void main(String[] args) {
		SpringApplication.run(BudgetApplication.class, args);
		System.out.println("✅ Основное приложение запущено на порту 8080");
	}
}
