package ru.kostyushin.diplom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kostyushin.diplom.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}