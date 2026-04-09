package org.example.bookingsystem.controller;

import jakarta.validation.Valid;
import org.example.bookingsystem.AuthResponse;
import org.example.bookingsystem.dto.LoginRequest;
import org.example.bookingsystem.dto.RegisterRequest;
import org.example.bookingsystem.entity.User;
import org.example.bookingsystem.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);

            // После регистрации сразу выдаём токен
            String token = userService.getToken(user.getLogin(), user.getRole());
            String fullName = userService.getFullname(user.getLastName(), user.getFirstName(), user.getMiddleName());

            return ResponseEntity.ok(new AuthResponse(token, user.getRole(), user.getId(), fullName));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByLogin(request.getLogin());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid credentials");
        }

        if(userService.isAccountTemporaryBanned(user)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Account is temporary banned for " + userService.getTemporaryBanTime() + " minutes. Try later");
        }

        if(!userService.checkPassword(request.getPassword(), user.getPasswordHash())){
            userService.incrementFailedAttempts(user);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid password");
        }
        userService.resetFailedAttempts(user);

        String token = userService.getToken(user.getLogin(), user.getRole());
        String fullName = userService.getFullname(user.getLastName(), user.getFirstName(), user.getMiddleName());

        return ResponseEntity.ok(new AuthResponse(token, user.getRole(), user.getId(), fullName));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        return ResponseEntity.ok("{\"message\": \"Logged out successfully. Please remove token from client storage.\"}");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String login = userDetails.getUsername();
            userService.deleteUser(login);
            return ResponseEntity.ok("{\"message\": \"Account:" + login + " deleted successfully\"}");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
