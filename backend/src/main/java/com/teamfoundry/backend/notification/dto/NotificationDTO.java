package com.teamfoundry.backend.notification.dto;

import com.teamfoundry.backend.notification.enums.NotificationType;
import lombok.Data;
import java.time.Instant;

@Data
public class NotificationDTO {
    private Integer id;
    private String message;
    private NotificationType type;
    private boolean isRead;
    private Integer relatedEntityId;
    private Instant createdAt;
}
