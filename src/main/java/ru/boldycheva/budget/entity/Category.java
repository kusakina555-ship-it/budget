package ru.boldycheva.budget.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "expense_type", nullable = false, length = 50)
    private ExpenseType expenseType;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    // constructors, getters, setters
}

enum ExpenseType {
    ENTERTAINMENT,     // Развлечения
    GROCERIES,         // Продукты
    UTILITIES,         // Бытовые расходы
    RENT,              // Арендная плата
    TRANSPORT,         // Транспорт
    INCOME,            // Доходы
    INVESTMENT         // Инвестиции
}