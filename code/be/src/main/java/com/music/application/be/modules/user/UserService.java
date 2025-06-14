package com.music.application.be.modules.user;

import com.music.application.be.modules.cloudinary.CloudinaryService;
import com.music.application.be.modules.user.dto.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    @Cacheable(value = "users", key = "#userId")
    public UserDetailDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return convertToDetailDTO(user);
    }

    @CachePut(value = "users", key = "#userId")
    @CacheEvict(value = {"allUsers", "followedArtists", "searchedFollowedArtists"}, allEntries = true)
    public UserDetailDTO updateUser(Long userId, UserUpdateDTO userDTO, MultipartFile avatarFile) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (userDTO.getUsername() != null && !userDTO.getUsername().trim().isEmpty()) {
            user.setUsername(userDTO.getUsername());
        }
        if (userDTO.getPhone() != null && !userDTO.getPhone().trim().isEmpty()) {
            user.setPhone(userDTO.getPhone());
        }
        if (userDTO.getEmail() != null && !userDTO.getEmail().trim().isEmpty()) {
            user.setEmail(userDTO.getEmail());
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarUrl = cloudinaryService.uploadFile(avatarFile, "image");
            user.setAvatar(avatarUrl);
        }

        User updatedUser = userRepository.save(user);
        return convertToDetailDTO(updatedUser);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @CacheEvict(value = {"users", "allUsers", "followedArtists", "searchedFollowedArtists"}, key = "#userId")
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        userRepository.delete(user);
    }

    // Các phương thức chuyển đổi DTO
    private UserDetailDTO convertToDetailDTO(User user) {
        return UserDetailDTO.builder()
                .id(user.getId())
                .role(user.getRole())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private UserResponseDTO convertToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .role(user.getRole())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .build();
    }
}