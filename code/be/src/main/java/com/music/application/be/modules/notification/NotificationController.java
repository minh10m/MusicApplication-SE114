package com.music.application.be.modules.notification;

import com.music.application.be.modules.notification.dto.CreateNotificationRequest;
import com.music.application.be.modules.notification.dto.NotificationDto;
import com.music.application.be.modules.notification.dto.NotificationResponse;
import com.music.application.be.modules.user.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }


    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{userId}")
    public ResponseEntity<NotificationResponse> createUserNotification(
            @PathVariable Long userId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam NotificationType type) {
        NotificationResponse response = notificationService.createNotificationForUser(userId, title, content, type);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyNotifications() {
        try {
            List<NotificationDto> notifications = notificationService.getMyNotifications();
            return ResponseEntity.ok(notifications);
        } catch (UsernameNotFoundException | EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch notifications");
        }
    }

    @GetMapping("/{userId}/paginated")
    public ResponseEntity<?> getUserNotificationsPaginated(
            @PathVariable Long userId,
            Pageable pageable) {
        try {
            Page<Notification> notifications = notificationService.getUserNotificationsPaginated(userId, pageable);
            return ResponseEntity.ok(notifications);
        } catch (Error e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Error("Failed to fetch paginated notifications"));
        }
    }

    @GetMapping("/{userId}/unread")
    public ResponseEntity<?> getUnreadNotifications(@PathVariable Long userId) {
        try {
            List<Notification> notifications = notificationService.getUnreadNotifications(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Error("Failed to fetch unread notifications"));
        }
    }

    @GetMapping("/{userId}/unread/count")
    public ResponseEntity<?> countUnreadNotifications(@PathVariable Long userId) {
        try {
            long count = notificationService.countUnreadNotifications(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Error("Failed to count unread notifications"));
        }
    }

    @PutMapping("/{userId}/mark-all-read")
    public ResponseEntity<?> markAllAsRead(@PathVariable Long userId) {
        try {
            int updatedCount = notificationService.markAllAsRead(userId);
            return ResponseEntity.ok(updatedCount);
        } catch (Error e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Error("Failed to mark notifications as read"));
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteAllByUserId(@PathVariable Long userId) {
        try {
            int deletedCount = notificationService.deleteAllByUserId(userId);
            return ResponseEntity.ok(deletedCount);
        } catch (Error e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Error("Failed to delete notifications"));
        }
    }

    @GetMapping("/{id}/user/{userId}")
    public ResponseEntity<?> getNotificationByIdAndUserId(
            @PathVariable Long id,
            @PathVariable Long userId) {
        try {
            return notificationService.getNotificationByIdAndUserId(id, userId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Error("Failed to fetch notification"));
        }
    }
}