package org.example.bookingsystem.repository;

import jakarta.transaction.Transactional;
import org.example.bookingsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    @Modifying
    @Transactional
    @Query("update User u set u.failedAttempt = u.failedAttempt + 1 where u.login = :login")
    void incrementFailedAttempts(@Param("login") String login);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.failedAttempt = 0 WHERE u.login = :login")
    void resetFailedAttempts(@Param("login") String login);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.temporaryBan = :timeBan WHERE u.login = :login")
    void setTemporaryBan(
            @Param("login") String login,
            @Param("timeBan") LocalDateTime timeBan);

    @Transactional
    @Query("select u.temporaryBan from User u where u.login = :login")
    LocalDateTime checkTemporaryBan(@Param("login") String login);
}
