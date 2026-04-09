package org.example.bookingsystem.repository;

import org.example.bookingsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
//определяем методы для сущности User
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmail(String email);
    Optional<User> findById(long id);

    boolean findByLoginAndIsActive(String login, boolean isActive);

    boolean existsByLogin(String login);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsById(long id);
}
