package com.music.application.be.modules.notification;

import com.music.application.be.modules.notification.dto.CreateNotificationRequest;
import com.music.application.be.modules.notification.dto.NotificationResponse;
import com.music.application.be.modules.user.User;
import com.music.application.be.modules.user.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @CachePut(value = "notifications", key = "#result.id")
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new Error("User not found with id: " + request.getUserId()));

        Notification notification = Notification.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .type(request.getType())
                .read(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(savedNotification);
    }

    @Transactional
    @CachePut(value = "notifications", key = "#result.id")
    public NotificationResponse createNotificationForUser(Long userId, String title, String content, NotificationType type) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(userId);
        request.setTitle(title);
        request.setContent(content);
        request.setType(type);
        return createNotification(request);
    }

    @Cacheable(value = "userNotifications", key = "#userId")
    public List<Notification> getUserNotifications(Long userId) {
        try {
            return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        } catch (Exception e) {
            // Log the error (you should add proper logging here)
            System.err.println("Error fetching user notifications: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Cacheable(value = "userNotificationsPaginated", key = "#userId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Notification> getUserNotificationsPaginated(Long userId, Pageable pageable) {
        try {
            return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        } catch (Exception e) {
            // Log the error
            System.err.println("Error fetching paginated notifications: " + e.getMessage());
            throw new Error("Failed to fetch paginated notifications", e);
        }
    }

    @Cacheable(value = "unreadNotifications", key = "#userId")
    public List<Notification> getUnreadNotifications(Long userId) {
        try {
            return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        } catch (Exception e) {
            // Log the Book error
            System.err.println("Error fetching unread notifications: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Cacheable(value = "unreadNotificationsCount", key = "#userId")
    public long countUnreadNotifications(Long userId) {
        try {
            return notificationRepository.countByUserIdAndReadFalse(userId);
        } catch (Exception e) {
            // Log the error
            System.err.println("Error counting unread notifications: " + e.getMessage());
            return 0;
        }
    }

    @Transactional
    @CacheEvict(value = {"userNotifications", "userNotificationsPaginated", "unreadNotifications", "unreadNotificationsCount"}, key = "#userId")
    public int markAllAsRead(Long userId) {
        try {
            return notificationRepository.markAllAsRead(userId);
        } catch (Exception e) {
            // Log the error
            System.err.println("Error marking notifications as read: " + e.getMessage());
            throw new Error("Failed to mark notifications as read", e);
        }
    }

    @Transactional
    @CacheEvict(value = {"userNotifications", "userNotificationsPaginated", "unreadNotifications", "unreadNotificationsCount"}, key = "#userId")
    public int deleteAllByUserId(Long userId) {
        try {
            return notificationRepository.deleteAllByUserId(userId);
        } catch (Exception e) {
            // Log the error
            System.err.println("Error deleting user notifications: " + e.getMessage());
            throw new Error("Failed to delete notifications", e);
        }
    }

    @Cacheable(value = "notifications", key = "#id + '-' + #userId")
    public Optional<Notification> getNotificationByIdAndUserId(Long id, Long userId) {
        try {
            return notificationRepository.findByIdAndUserId(id, userId);
        } catch (Exception e) {
            // Log the error
            System.err.println("Error fetching notification by ID: " + e.getMessage());
            return Optional.empty();
        }
    }
}