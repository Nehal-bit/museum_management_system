package com.museum.controller;

import com.museum.model.Guide;
import com.museum.model.Notification;
import com.museum.repository.GuideRepository;
import com.museum.service.GuideAssignmentService;
import com.museum.service.NotificationManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/guides")
public class GuideController {

    private final GuideRepository guideRepository;
    private final GuideAssignmentService assignmentService;
    private final NotificationManager notificationManager;

    public GuideController(GuideRepository guideRepository,
                           GuideAssignmentService assignmentService,
                           NotificationManager notificationManager) {
        this.guideRepository = guideRepository;
        this.assignmentService = assignmentService;
        this.notificationManager = notificationManager;
    }

    @GetMapping({"", "/"})
    public ResponseEntity<?> getAllGuides() {
        List<Guide> guides = guideRepository.findAll();
        List<Map<String, Object>> result = guides.stream().map(g -> Map.<String, Object>of(
                "userId", g.getUserId(), "id", g.getUserId(), "name", g.getName(),
                "email", g.getEmail(),
                "specialization", g.getSpecialization() != null ? g.getSpecialization() : "",
                "available", g.isAvailable()
        )).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return guideRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Guide's own assignments (with full booking details) */
    @GetMapping("/{id}/assignments")
    public ResponseEntity<?> getAssignments(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByGuideFlat(id));
    }

    /** Guide confirms an assignment */
    @PutMapping("/assignments/{assignmentId}/confirm")
    public ResponseEntity<?> confirmAssignment(@PathVariable Long assignmentId) {
        try {
            return ResponseEntity.ok(assignmentService.confirmAssignment(assignmentId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Guide rejects an assignment */
    @PutMapping("/assignments/{assignmentId}/reject")
    public ResponseEntity<?> rejectAssignment(@PathVariable Long assignmentId) {
        try {
            return ResponseEntity.ok(assignmentService.rejectAssignment(assignmentId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Guide's confirmed schedule */
    @GetMapping("/{id}/schedule")
    public ResponseEntity<?> getSchedule(@PathVariable Long id) {
        List<Map<String, Object>> all = assignmentService.getAssignmentsByGuideFlat(id);
        List<Map<String, Object>> confirmed = all.stream()
                .filter(a -> "CONFIRMED".equals(a.get("status"))).toList();
        return ResponseEntity.ok(Map.of("guideId", id, "confirmedAssignments", confirmed,
                "totalConfirmed", confirmed.size()));
    }

    @GetMapping("/{id}/notifications")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable Long id) {
        return ResponseEntity.ok(notificationManager.getNotificationsForUser(id));
    }
}
