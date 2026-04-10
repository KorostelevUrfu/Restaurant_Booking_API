package org.example.bookingsystem.service;

import org.example.bookingsystem.CustomUserDetails;
import org.example.bookingsystem.JWTUtil;
import org.example.bookingsystem.dto.RegisterRequest;
import org.example.bookingsystem.dto.UpdatePasswordRequest;
import org.example.bookingsystem.dto.UpdateRequest;
import org.example.bookingsystem.entity.User;
import org.example.bookingsystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    //время блокировки пользователя при 3 неудачных попытках входа в систему
    private final int TEMPORARY_BAN_MINUTES = 1;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JWTUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public int getTemporaryBanTime(){
        return TEMPORARY_BAN_MINUTES;
    }


    public User register(RegisterRequest request) {

        String middleName = request.getMiddleName();
        if (middleName != null && !middleName.isEmpty()) {
            if (!middleName.matches("^[А-Яа-яA-Za-z-]+$")) {
                throw new RuntimeException("Middle name contains invalid characters");
            }
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        if (userRepository.existsByLogin(request.getLogin())) {
            throw new RuntimeException("Login already exists");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User(
                request.getLogin(),
                passwordEncoder.encode(request.getPassword()),
                request.getLastName(),
                request.getFirstName(),
                request.getMiddleName(),
                request.getPhone(),
                request.getEmail()
        );

        return userRepository.save(user);
    }

    //назначение роли
    public User assignRole(Long userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));


        if (!newRole.equals("client") && !newRole.equals("manager") && !newRole.equals("admin")) {
            throw new RuntimeException("Invalid role. Allowed: client, manager, admin");
        }

        user.setRole(newRole);
        return userRepository.save(user);
    }

    //получение всех пользователей (admin)
    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    //получение токена
    public String getToken(User user){
        return jwtUtil.generateToken(user);
    }

    public String getFullname(String lastName, String firstName, String middleName){
        String fullName = lastName + " " + firstName;
        if (middleName != null && !middleName.isEmpty()) {
            fullName += " " + middleName;
        }
        return fullName;
    }

    public User findByLogin(String login) {
        return userRepository.findByLogin(login).orElse(null);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public void incrementFailedAttempts(User user){
        userRepository.incrementFailedAttempts(user.getLogin());

        //проверяем не достиг ли лимита
        User updatedUser = userRepository.findByLogin(user.getLogin()).orElse(user);

        //хз почему такое ограничение работает но главное что работает
        if (updatedUser.getFailedAttempt() >= 1) {
            temporaryBan(updatedUser);
        }
    }

    public void resetFailedAttempts(User user){
        userRepository.resetFailedAttempts(user.getLogin());
    }

    private void temporaryBan(User user){
        LocalDateTime temporaryBan = LocalDateTime.now().plusMinutes(TEMPORARY_BAN_MINUTES);
        userRepository.setTemporaryBan(user.getLogin(), temporaryBan);
    }

    public boolean isAccountTemporaryBanned(User user){
        if (user == null) return false;

        LocalDateTime temporaryBanForUser = userRepository.checkTemporaryBan(user.getLogin());
        if(temporaryBanForUser == null) return false;
        if(LocalDateTime.now().isAfter(temporaryBanForUser)){
            resetFailedAttempts(user);
            userRepository.setTemporaryBan(user.getLogin(), null);
            return false;
        }
        return true;
    }

    @Transactional
    public void changePassword(UUID publicId, UpdatePasswordRequest request) {

        // 1. Получаем пользователя
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Проверяем текущий пароль
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // 3. Проверяем, что новый пароль совпадает с подтверждением
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("New passwords do not match");
        }

        // 4. Проверяем, что новый пароль отличается от старого
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new RuntimeException("New password must be different from current password");
        }

        // 5. Обновляем пароль
        int updatedRows = userRepository.updatePassword(
                passwordEncoder.encode(request.getNewPassword()),
                publicId
        );

        // 6. Проверяем, что обновление произошло
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to update password");
        }
    }

    public User updateUserInfo(CustomUserDetails userDetails, UpdateRequest request) {
        UUID publicId = userDetails.getPublicId();

        //обновляем поля которые не null
        User user = findByPublicId(publicId);

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getMiddleName() != null) {
            user.setMiddleName(request.getMiddleName());
        }
        if (request.getPhone() != null) {
            if (userRepository.existsByPhone(request.getPhone())) {
                User existing = userRepository.findByPhone(request.getPhone()).get();
                if (existing.getPublicId() != publicId) {
                    throw new RuntimeException("Phone already taken");
                }
            }
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            if (userRepository.existsByEmail(request.getEmail())) {
                User existing = userRepository.findByEmail(request.getEmail()).get();
                if (existing.getPublicId() != publicId) {
                    throw new RuntimeException("Email already taken");
                }
            }
            user.setEmail(request.getEmail());
        }
        userRepository.save(user);
        return user;
    }

    public void deleteUserByPublicId(UUID publicId) {
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsActive(false);
        userRepository.save(user);
    }

    public User findByPublicId(UUID pubicId){
        return userRepository.findByPublicId(pubicId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}