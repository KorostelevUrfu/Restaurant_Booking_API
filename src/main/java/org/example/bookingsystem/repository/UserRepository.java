package org.example.bookingsystem.repository;

import jakarta.transaction.Transactional;
import org.example.bookingsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

//определяем методы для сущности User
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmail(String email);
    Optional<User> findById(long id);
    Optional<User> findByPublicId(UUID publicId);

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
    @Query("update User u set u.failedAttempt = 0 where u.login = :login")
    void resetFailedAttempts(@Param("login") String login);

    @Modifying
    @Transactional
    @Query("update User u set u.temporaryBan = :timeBan where u.login = :login")
    void setTemporaryBan(
            @Param("login") String login,
            @Param("timeBan") LocalDateTime timeBan);

    @Transactional
    @Query("select u.temporaryBan from User u where u.login = :login")
    LocalDateTime checkTemporaryBan(@Param("login") String login);

    @Modifying
    @Transactional
    @Query("update User u set u.email = :email, u.phone = :phone, u.lastName = :lastName, u.firstName = :firstName, u.middleName = :middleName where u.publicId = :publicId")
    void updateClientInfo(@Param("email") String email,
                          @Param("phone") String phone,
                          @Param("lastName") String lastName,
                          @Param("firstName") String firstName,
                          @Param("middleName") String middleName,
                          @Param("publicId") UUID publicId);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.passwordHash = :newPasswordHash WHERE u.publicId = :publicId")
    int updatePassword(@Param("newPasswordHash") String newPasswordHash,
                       @Param("publicId") UUID publicId);
}
