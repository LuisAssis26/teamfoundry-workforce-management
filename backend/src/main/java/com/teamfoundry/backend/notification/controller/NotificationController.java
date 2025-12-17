package com.teamfoundry.backend.notification.controller;

import com.teamfoundry.backend.notification.dto.NotificationDTO;
import com.teamfoundry.backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(NotificationController.class);

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        LOGGER.info("Fetching notifications for user: {}", userDetails.getUsername());
        List<NotificationDTO> result = notificationService.getUserNotifications(userDetails.getUsername());
        LOGGER.info("Found {} notifications for user: {}", result.size(), userDetails.getUsername());
        return ResponseEntity.ok(result); 
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
