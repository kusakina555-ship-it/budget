package ru.boldycheva.budget.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        System.out.println("=== Правильные BCrypt хеши ===");
        System.out.println("Пароль '111': " + encoder.encode("111"));
        System.out.println("Пароль '222': " + encoder.encode("222"));
        System.out.println("Пароль '333': " + encoder.encode("333"));
    }
}
