package com.teamfoundry.backend.notification.repository;

import com.teamfoundry.backend.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);
    long countByUserIdAndIsReadFalse(Integer userId);
}
