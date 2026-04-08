package com.museum.controller;

import com.museum.model.Exhibit;
import com.museum.model.Notification;
import com.museum.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/visitors")
public class VisitorController {

    private final ExhibitService exhibitService;
    private final BookingService bookingService;
    private final FeedbackService feedbackService;
    private final NotificationManager notificationManager;

    public VisitorController(ExhibitService exhibitService, BookingService bookingService,
                             FeedbackService feedbackService, NotificationManager notificationManager) {
        this.exhibitService = exhibitService;
        this.bookingService = bookingService;
        this.feedbackService = feedbackService;
        this.notificationManager = notificationManager;
    }

    @GetMapping("/{id}/exhibits")
    public ResponseEntity<List<Exhibit>> browseExhibits(@PathVariable Long id) {
        return ResponseEntity.ok(exhibitService.getActiveExhibits());
    }

    @GetMapping("/{id}/bookings")
    public ResponseEntity<?> getMyBookings(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingsByVisitorFlat(id));
    }

    @GetMapping("/{id}/notifications")
    public ResponseEntity<List<Notification>> getMyNotifications(@PathVariable Long id) {
        return ResponseEntity.ok(notificationManager.getNotificationsForUser(id));
    }

    @GetMapping("/{id}/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("visitorId", id,
                "unreadNotifications", notificationManager.countUnread(id)));
    }
}
