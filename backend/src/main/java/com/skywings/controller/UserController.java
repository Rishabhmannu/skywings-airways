package com.skywings.controller;

import com.skywings.dto.response.UserProfileResponse;
import com.skywings.entity.User;
import com.skywings.service.UserService;
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
        return ResponseEntity.ok(userService.updateProfile(user,
            updates.get("name"), updates.get("phone")));
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@AuthenticationPrincipal User user,
                                                               @RequestBody Map<String, String> passwords) {
        userService.changePassword(user,
            passwords.get("currentPassword"), passwords.get("newPassword"));
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}
