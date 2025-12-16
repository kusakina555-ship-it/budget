package ru.boldycheva.budget.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.boldycheva.budget.entity.Category;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    boolean existsByName(String name);
    List<Category> findByCategoryType(String categoryType);
    List<Category> findByParentIsNull();
    List<Category> findByCategoryTypeAndParentIsNull(String categoryType);
    List<Category> findByParentId(Long parentId);
}
