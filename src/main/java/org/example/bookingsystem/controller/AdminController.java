package org.example.bookingsystem.controller;

import org.example.bookingsystem.entity.User;
import org.example.bookingsystem.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")  // Только администраторы
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    //получить всех пользователей
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    //назначить роль пользователю
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<User> assignRole(
            @PathVariable Long userId,
            @RequestParam String role) {
        User user = userService.assignRole(userId, role);
        return ResponseEntity.ok(user);
    }

//    //удалить пользователя по логину
//    @DeleteMapping("/users/{userLogin}")
//    public ResponseEntity<?> deleteUser(@PathVariable String userLogin) {
//        User user = userService.findByLogin(userLogin);
//        if (user == null) {
//            return ResponseEntity.badRequest().body("{\"error\": \"User not found\"}");
//        }
//        userService.deleteUser(user.getLogin());
//        return ResponseEntity.ok("{\"message\": \"User deleted\"}");
//    }
}
