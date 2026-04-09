package org.example.bookingsystem.controller;

import org.example.bookingsystem.AuthResponse;
import org.example.bookingsystem.JWTUtil;
import org.example.bookingsystem.dto.LoginRequest;
import org.example.bookingsystem.dto.RegisterRequest;
import org.example.bookingsystem.entity.User;
import org.example.bookingsystem.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JWTUtil jwtUtil;

    public AuthController(UserService userService, JWTUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);

            // После регистрации сразу выдаём токен
            String token = jwtUtil.generateToken(user.getLogin(), user.getRole());
            String fullName = user.getLastName() + " " + user.getFirstName();
            if (user.getMiddleName() != null && !user.getMiddleName().isEmpty()) {
                fullName += " " + user.getMiddleName();
            }

            return ResponseEntity.ok(new AuthResponse(token, user.getRole(), user.getId(), fullName));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        User user = userService.findByLogin(request.getLogin());

        if (user == null || !userService.checkPassword(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = jwtUtil.generateToken(user.getLogin(), user.getRole());

        String fullName = user.getLastName() + " " + user.getFirstName();
        if (user.getMiddleName() != null && !user.getMiddleName().isEmpty()) {
            fullName += " " + user.getMiddleName();
        }

        return ResponseEntity.ok(new AuthResponse(token, user.getRole(), user.getId(), fullName));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        return ResponseEntity.ok("{\"message\": \"Logged out successfully. Please remove token from client storage.\"}");
    }

    @DeleteMapping("/delete/{login}")
    public ResponseEntity<?> deleteAccount(@PathVariable("login") String login) {
        try {
            userService.deleteUser(login);
            return ResponseEntity.ok("{\"message\": \"Account deleted successfully\"}");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
