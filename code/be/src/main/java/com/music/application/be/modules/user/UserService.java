package com.music.application.be.modules.user;

import com.music.application.be.modules.cloudinary.CloudinaryService;
import com.music.application.be.modules.favorite_playlist.FavoritePlaylistRepository;
import com.music.application.be.modules.favorite_song.FavoriteSongRepository;
import com.music.application.be.modules.follow_artist.FollowArtistRepository;
import com.music.application.be.modules.playlist.PlaylistRepository;
import com.music.application.be.modules.user.dto.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private final FavoriteSongRepository favoriteSongRepository;
    private final FollowArtistRepository followArtistRepository;
    private final FavoritePlaylistRepository favoritePlaylistRepository;

    @Cacheable(value = "users", key = "#userId")
    public UserDetailDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return convertToDetailDTO(user);
    }

    public ProfileDTO getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new UsernameNotFoundException("User not authenticated");
        }

        User fullUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        int favoriteSongCount = favoriteSongRepository.countByUserId(fullUser.getId());
        int followedArtistCount = followArtistRepository.countByUserId(fullUser.getId());
        int playlistCount = favoritePlaylistRepository.countByUserId(fullUser.getId());

        return new ProfileDTO(
                fullUser.getUsername(),
                fullUser.getEmail(),
                fullUser.getPhone(),
                fullUser.getAvatar(),
                favoriteSongCount,
                followedArtistCount,
                playlistCount
        );
    }

    @CacheEvict(value = {"allUsers", "followedArtists", "searchedFollowedArtists"}, allEntries = true)
    public UserDetailDTO updateCurrentUser(UserUpdateDTO userDTO, MultipartFile avatarFile) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            throw new UsernameNotFoundException("User not authenticated");
        }

        if (userDTO.getUsername() != null && !userDTO.getUsername().trim().isEmpty()) {
            currentUser.setUsername(userDTO.getUsername());
        }
        if (userDTO.getPhone() != null && !userDTO.getPhone().trim().isEmpty()) {
            currentUser.setPhone(userDTO.getPhone());
        }
        if (userDTO.getEmail() != null && !userDTO.getEmail().trim().isEmpty()) {
            currentUser.setEmail(userDTO.getEmail());
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarUrl = cloudinaryService.uploadFile(avatarFile, "image");
            currentUser.setAvatar(avatarUrl);
        }

        User updated = userRepository.save(currentUser);
        return convertToDetailDTO(updated);
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