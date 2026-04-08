package com.museum.controller;

import com.museum.dto.LoginDTO;
import com.museum.dto.RegisterDTO;
import com.museum.model.User;
import com.museum.model.UserRole;
import com.museum.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Public registration: VISITOR only.
 * Guide registration: Admin-only via /api/admin/guides/create.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO dto) {
        // Block guide self-registration — guides are created by admin only
        if (dto.getRole() == UserRole.GUIDE || dto.getRole() == UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Public registration is only available for Visitors. Guides are registered by Admin."));
        }
        try {
            User user = userService.register(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Registration successful",
                    "userId", user.getUserId(),
                    "id",     user.getUserId(),
                    "name",   user.getName(),
                    "email",  user.getEmail(),
                    "role",   user.getRole()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO dto) {
        try {
            User user = userService.login(dto);
            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "userId",  user.getUserId(),
                    "id",      user.getUserId(),
                    "name",    user.getName(),
                    "email",   user.getEmail(),
                    "role",    user.getRole()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
