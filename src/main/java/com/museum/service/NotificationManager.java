package com.museum.service;

import com.museum.model.Notification;
import com.museum.model.NotificationStatus;
import com.museum.model.User;
import com.museum.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class NotificationManager {

    private static NotificationManager instance;
    private final NotificationRepository notificationRepository;

    public NotificationManager(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
        if (instance == null) instance = this;
    }

    public static NotificationManager getInstance() { return instance; }

    @Transactional
    public Notification notifyUser(User user, String message) {
        Notification n = new Notification();
        n.setUser(user); n.setMessage(message); n.setStatus(NotificationStatus.SENT);
        return notificationRepository.save(n);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUser_UserIdOrderByCreatedOnDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUser_UserIdAndStatus(userId, NotificationStatus.SENT);
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUser_UserIdAndStatus(userId, NotificationStatus.SENT);
    }

    @Transactional
    public Notification markAsRead(Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        n.setStatus(NotificationStatus.READ);
        return notificationRepository.save(n);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = getUnreadNotifications(userId);
        unread.forEach(n -> n.setStatus(NotificationStatus.READ));
        notificationRepository.saveAll(unread);
    }
}
