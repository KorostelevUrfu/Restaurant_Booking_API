package org.example.bookingsystem.controller;

import jakarta.validation.Valid;
import org.example.bookingsystem.CustomUserDetails;
import org.example.bookingsystem.dto.UpdatePasswordRequest;
import org.example.bookingsystem.response.AuthResponse;
import org.example.bookingsystem.dto.LoginRequest;
import org.example.bookingsystem.dto.RegisterRequest;
import org.example.bookingsystem.dto.UpdateRequest;
import org.example.bookingsystem.entity.User;
import org.example.bookingsystem.repository.UserRepository;
import org.example.bookingsystem.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;

    public AuthController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);

            // После регистрации сразу выдаём токен
            String token = userService.getToken(user);
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

        String token = userService.getToken(user);
        String fullName = userService.getFullname(user.getLastName(), user.getFirstName(), user.getMiddleName());

        return ResponseEntity.ok(new AuthResponse(token, user.getRole(), user.getId(), fullName));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        return ResponseEntity.ok("{\"message\": \"Logged out successfully. Please remove token from client storage.\"}");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAccount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID publicId = userDetails.getPublicId();  // ← получаем UUID!
        userService.deleteUserByPublicId(publicId);
        return ResponseEntity.ok("{\"message\": \"Account deleted successfully\"}");
    }

    @PatchMapping("/update/password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdatePasswordRequest request) {

        userService.changePassword(userDetails.getPublicId(), request);

        return ResponseEntity.ok("Password changed successfully");
    }

    @PatchMapping("/update/user")
    public ResponseEntity<?> updateUserInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateRequest request) {

        try {
            userService.updateUserInfo(userDetails, request);
            return ResponseEntity.ok("Информация обновлена");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
