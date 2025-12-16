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

    @Column(name = "category_type", nullable = false, length = 10)
    private String categoryType; // "INCOME" или "EXPENSE"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Category> subcategories = new ArrayList<>();

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    public Category() {}

    public Category(String name, String categoryType) {
        this.name = name;
        this.categoryType = categoryType;
    }

    public Category(String name, String categoryType, Category parent) {
        this.name = name;
        this.categoryType = categoryType;
        this.parent = parent;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategoryType() { return categoryType; }
    public void setCategoryType(String categoryType) { this.categoryType = categoryType; }

    public Category getParent() { return parent; }
    public void setParent(Category parent) { this.parent = parent; }

    public List<Category> getSubcategories() { return subcategories; }
    public void setSubcategories(List<Category> subcategories) { this.subcategories = subcategories; }

    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }

    // Вспомогательные методы
    public boolean isTopLevel() {
        return parent == null;
    }

    public boolean isSubcategory() {
        return parent != null;
    }
}
