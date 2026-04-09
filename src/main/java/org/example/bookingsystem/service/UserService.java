package org.example.bookingsystem.service;

import org.example.bookingsystem.JWTUtil;
import org.example.bookingsystem.dto.RegisterRequest;
import org.example.bookingsystem.entity.User;
import org.example.bookingsystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    //время блокировки пользователя при 3 неудачных попытках входа в систему
    private final int TEMPORARY_BAN_MINUTES = 10;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JWTUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public User register(RegisterRequest request) {

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

    public void deleteUser(String login) {
        User user = findByLogin(login);
        if (user == null || !user.getIsActive()) {
            throw new RuntimeException("User not found");
        }
        //просто делаем пометку что пользователь деактивирован
        user.setIsActive(false);
    }

    //получение токена
    public String getToken(String login, String role){
        return jwtUtil.generateToken(login, role);
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

        if (updatedUser.getFailedAttempt() >= 2) {
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
}