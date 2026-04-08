package com.museum.repository;

import com.museum.model.Notification;
import com.museum.model.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUser_UserIdOrderByCreatedOnDesc(Long userId);

    List<Notification> findByUser_UserIdAndStatus(Long userId, NotificationStatus status);

    long countByUser_UserIdAndStatus(Long userId, NotificationStatus status);
}
