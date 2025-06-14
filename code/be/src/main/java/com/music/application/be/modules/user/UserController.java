package com.music.application.be.modules.user;

import com.music.application.be.modules.user.dto.UserDetailDTO;
import com.music.application.be.modules.user.dto.UserResponseDTO;
import com.music.application.be.modules.user.dto.UserUpdateDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    private ObjectMapper objectMapper; // Inject từ Spring (có sẵn)

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Get user by ID",
            description = "Retrieves details of a specific user by its ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved user",
                            content = @Content(schema = @Schema(implementation = UserDetailDTO.class))),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailDTO> getUserById(@PathVariable Long userId) {
        UserDetailDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "Update user with optional avatar",
            description = "Updates a user's details and optionally uploads a new avatar. Requires multipart/form-data with two parts: 'user' (JSON) and 'avatar' (file, optional).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully updated user",
                            content = @Content(schema = @Schema(implementation = UserDetailDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "415", description = "Unsupported Media Type")
            }
    )
    @PutMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDetailDTO> updateUser(
            @PathVariable Long userId,
            @Parameter(description = "Thông tin người dùng", schema = @Schema(implementation = UserUpdateDTO.class))
            @RequestPart("user") String userJson,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) throws IOException {
        UserUpdateDTO userUpdateDTO = objectMapper.readValue(userJson, UserUpdateDTO.class);
        UserDetailDTO updatedUser = userService.updateUser(userId, userUpdateDTO, avatarFile);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all users.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved users",
                            content = @Content(schema = @Schema(implementation = List.class)))
            }
    )
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(
            summary = "Delete a user",
            description = "Deletes a user by its ID.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Successfully deleted user"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}