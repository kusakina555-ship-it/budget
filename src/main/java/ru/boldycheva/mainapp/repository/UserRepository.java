package ru.boldycheva.mainapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.boldycheva.mainapp.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String userName);
    Optional<User> findByEmail(String email);
    boolean existsByUserName(String userName);
    boolean existsByEmail(String email);
}