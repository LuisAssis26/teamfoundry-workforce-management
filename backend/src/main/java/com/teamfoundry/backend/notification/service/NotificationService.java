package com.teamfoundry.backend.notification.service;

import com.teamfoundry.backend.account.model.Account;
import com.teamfoundry.backend.account.repository.AccountRepository;
import com.teamfoundry.backend.notification.dto.NotificationDTO;
import com.teamfoundry.backend.notification.enums.NotificationType;
import com.teamfoundry.backend.notification.model.Notification;
import com.teamfoundry.backend.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final AccountRepository accountRepository;
    
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(NotificationService.class);

    @Transactional
    public void createNotification(Account user, String message, NotificationType type, Integer relatedEntityId) {
        LOGGER.info("Creating notification for user: {}, type: {}", user.getEmail(), type);
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setRead(false);
        notificationRepository.saveAndFlush(notification);
    }
    
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(String email) {
        Account user = accountRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    public void markAsRead(Integer notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllAsRead(String email) {
        Account user = accountRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        List<Notification> unread = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .filter(n -> !n.isRead())
                .collect(Collectors.toList());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    private NotificationDTO mapToDTO(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(n.getId());
        dto.setMessage(n.getMessage());
        dto.setType(n.getType());
        dto.setRead(n.isRead());
        dto.setRelatedEntityId(n.getRelatedEntityId());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}
