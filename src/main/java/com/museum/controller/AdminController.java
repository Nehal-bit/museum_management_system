package com.museum.controller;

import com.museum.dto.AnalyticsDTO;
import com.museum.model.*;
import com.museum.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final GuideAssignmentService assignmentService;
    private final BookingService bookingService;
    private final ReportService reportService;
    private final AnalyticsService analyticsService;
    private final ExhibitService exhibitService;

    public AdminController(UserService userService,
                           GuideAssignmentService assignmentService,
                           BookingService bookingService,
                           ReportService reportService,
                           AnalyticsService analyticsService,
                           ExhibitService exhibitService) {
        this.userService = userService;
        this.assignmentService = assignmentService;
        this.bookingService = bookingService;
        this.reportService = reportService;
        this.analyticsService = analyticsService;
        this.exhibitService = exhibitService;
    }

    // ── Analytics ─────────────────────────────────────────────────────────
    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsDTO> getAnalytics() {
        return ResponseEntity.ok(analyticsService.getDashboardAnalytics());
    }

    // ── Users ─────────────────────────────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ── Exhibits ──────────────────────────────────────────────────────────
    @PostMapping("/exhibits")
    public ResponseEntity<?> addExhibit(@RequestBody Exhibit exhibit) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(exhibitService.addExhibit(exhibit));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/exhibits/{id}")
    public ResponseEntity<?> updateExhibit(@PathVariable Long id, @RequestBody Exhibit exhibit) {
        try {
            return ResponseEntity.ok(exhibitService.updateExhibit(id, exhibit));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/exhibits/{id}")
    public ResponseEntity<?> deleteExhibit(@PathVariable Long id) {
        try {
            exhibitService.deleteExhibit(id);
            return ResponseEntity.ok(Map.of("message", "Exhibit archived successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/exhibits")
    public ResponseEntity<?> getAllExhibits() {
        return ResponseEntity.ok(exhibitService.getAllExhibits());
    }

    // ── Bookings (main admin workflow) ────────────────────────────────────

    /** All bookings — admin sees everything */
    @GetMapping({"/bookings", "/bookings/"})
    public ResponseEntity<?> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookingsFlat());
    }

    /** Pending bookings — needs admin action */
    @GetMapping("/bookings/pending")
    public ResponseEntity<?> getPendingBookings() {
        return ResponseEntity.ok(bookingService.getPendingBookingsFlat());
    }

    /** Admin confirms a booking → status CONFIRMED, ticket PAID */
    @PutMapping("/bookings/{id}/confirm")
    public ResponseEntity<?> confirmBooking(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(bookingService.confirmBooking(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Admin rejects a booking */
    @PutMapping("/bookings/{id}/reject")
    public ResponseEntity<?> rejectBooking(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(bookingService.rejectBooking(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Keep old schedule endpoints working for backward compatibility
    @GetMapping({"/schedules", "/schedules/"})
    public ResponseEntity<?> getAllSchedules() { return getAllBookings(); }
    @GetMapping("/schedules/pending")
    public ResponseEntity<?> getPendingSchedules() { return getPendingBookings(); }
    @PutMapping("/schedules/{id}/approve")
    public ResponseEntity<?> approveSchedule(@PathVariable Long id) { return confirmBooking(id); }
    @PutMapping("/schedules/{id}/reject")
    public ResponseEntity<?> rejectSchedule(@PathVariable Long id) { return rejectBooking(id); }

    // ── Guide Management ──────────────────────────────────────────────────

    /** Admin creates guide account */
    @PostMapping("/guides/create")
    public ResponseEntity<?> createGuide(@RequestBody Map<String, Object> body) {
        try {
            String name           = (String) body.get("name");
            String email          = (String) body.get("email");
            String password       = (String) body.get("password");
            String specialization = (String) body.getOrDefault("specialization", "General");
            if (name == null || email == null || password == null)
                return ResponseEntity.badRequest().body(Map.of("error", "name, email, password required"));

            com.museum.dto.RegisterDTO dto = new com.museum.dto.RegisterDTO();
            dto.setName(name); dto.setEmail(email); dto.setPassword(password);
            dto.setRole(UserRole.GUIDE); dto.setSpecialization(specialization);
            User guide = userService.register(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Guide created", "userId", guide.getUserId(),
                    "id", guide.getUserId(), "name", guide.getName(),
                    "email", guide.getEmail(), "role", guide.getRole()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** List all guides */
    @GetMapping({"/guides", "/guides/"})
    public ResponseEntity<?> getAllGuides() {
        List<Guide> guides = assignmentService.getAllGuides();
        List<Map<String, Object>> result = guides.stream().map(g -> Map.<String, Object>of(
                "userId", g.getUserId(), "id", g.getUserId(), "name", g.getName(),
                "email", g.getEmail(),
                "specialization", g.getSpecialization() != null ? g.getSpecialization() : "",
                "available", g.isAvailable()
        )).toList();
        return ResponseEntity.ok(result);
    }

    /**
     * Assign a guide to a specific BOOKING.
     * Body: { "guideId": 2, "bookingId": 5 }
     */
    @PostMapping("/guides/assign")
    public ResponseEntity<?> assignGuide(@RequestBody Map<String, Object> body) {
        try {
            Long guideId   = Long.valueOf(body.get("guideId").toString());
            Long bookingId = Long.valueOf(body.get("bookingId").toString());
            Map<String, Object> result = assignmentService.assignGuideToBooking(guideId, bookingId);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Delete a guide (blocked if active assignments exist) */
    @DeleteMapping("/guides/{id}")
    public ResponseEntity<?> deleteGuide(@PathVariable Long id) {
        try {
            userService.deleteGuide(id);
            return ResponseEntity.ok(Map.of("message", "Guide deleted successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** All guide assignments */
    @GetMapping("/guides/assignments")
    public ResponseEntity<?> getAllAssignments() {
        return ResponseEntity.ok(assignmentService.getAllAssignmentsFlat());
    }

    // ── Reports ───────────────────────────────────────────────────────────

    @PostMapping("/reports/generate")
    public ResponseEntity<?> generateReport(@RequestBody Map<String, Object> body) {
        try {
            String reportType = (String) body.get("reportType");
            Long adminId = Long.valueOf(body.get("adminId").toString());
            Map<String, Object> result = reportService.generateReport(reportType, adminId);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/reports")
    public ResponseEntity<?> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReportsFlat());
    }

    @GetMapping("/reports/type/{type}")
    public ResponseEntity<?> getReportsByType(@PathVariable String type) {
        return ResponseEntity.ok(reportService.getReportsByTypeFlat(type));
    }

    @DeleteMapping("/reports/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable Long id) {
        try {
            reportService.deleteReport(id);
            return ResponseEntity.ok(Map.of("message", "Report deleted successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
