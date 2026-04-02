package com.skywings.controller;

import com.skywings.dto.response.UserProfileResponse;
import com.skywings.entity.User;
import com.skywings.service.UserService;
import com.skywings.util.InputSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getProfile(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(@AuthenticationPrincipal User user,
                                                              @RequestBody Map<String, String> updates) {
        String name = updates.get("name");
        String phone = updates.get("phone");

        if (name != null) {
            name = InputSanitizer.stripHtml(name);
            if (name.length() < 2 || name.length() > 100) {
                throw new IllegalArgumentException("Name must be between 2 and 100 characters");
            }
        }
        if (phone != null && !phone.matches("^\\+?\\d{10,15}$")) {
            throw new IllegalArgumentException("Invalid phone number format");
        }

        return ResponseEntity.ok(userService.updateProfile(user, name, phone));
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@AuthenticationPrincipal User user,
                                                               @RequestBody Map<String, String> passwords) {
        String currentPassword = passwords.get("currentPassword");
        String newPassword = passwords.get("newPassword");

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("Current password is required");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters");
        }
        if (!newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#]).{8,}$")) {
            throw new IllegalArgumentException("Password must contain uppercase, lowercase, digit, and special character");
        }

        userService.changePassword(user, currentPassword, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}
