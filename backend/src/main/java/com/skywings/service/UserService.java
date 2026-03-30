package com.skywings.service;

import com.skywings.dto.response.UserProfileResponse;
import com.skywings.entity.User;
import com.skywings.exception.ResourceNotFoundException;
import com.skywings.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileResponse getProfile(User user) {
        return toProfileResponse(user);
    }

    public UserProfileResponse updateProfile(User user, String name, String phone) {
        if (name != null && !name.isBlank()) user.setName(name);
        if (phone != null && !phone.isBlank()) user.setPhone(phone);
        user = userRepository.save(user);
        return toProfileResponse(user);
    }

    public void changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // Admin
    public List<UserProfileResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::toProfileResponse)
            .toList();
    }

    public UserProfileResponse getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toProfileResponse(user);
    }

    private UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .role(user.getRole().name())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
