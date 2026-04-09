package org.example.bookingsystem.service;

import org.example.bookingsystem.dto.RegisterRequest;
import org.example.bookingsystem.entity.User;
import org.example.bookingsystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        userRepository.delete(user);
    }

    public User findByLogin(String login) {
        return userRepository.findByLogin(login).orElse(null);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}