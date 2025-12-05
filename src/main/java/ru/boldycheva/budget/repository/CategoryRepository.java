package ru.boldycheva.budget.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.boldycheva.budget.entity.Category;
import ru.boldycheva.budget.entity.ExpenseType;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    boolean existsByName(String name);
    List<Category> findByExpenseType(ExpenseType expenseType);
}
