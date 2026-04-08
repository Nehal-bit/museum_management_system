package com.museum.controller;

import com.museum.service.BookingService;
import com.museum.service.ScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Kept for backward compatibility.
 * New flow uses /api/bookings directly.
 * The /api/schedules/visitor/{id} endpoint now returns booking data.
 */
@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final BookingService bookingService;

    public ScheduleController(ScheduleService scheduleService, BookingService bookingService) {
        this.scheduleService = scheduleService;
        this.bookingService = bookingService;
    }

    /** Visitor's bookings — kept for frontend compat */
    @GetMapping("/visitor/{visitorId}")
    public ResponseEntity<?> getByVisitor(@PathVariable Long visitorId) {
        try {
            return ResponseEntity.ok(bookingService.getBookingsByVisitorFlat(visitorId));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(java.util.List.of());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllSchedules() {
        return ResponseEntity.ok(bookingService.getAllBookingsFlat());
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPending() {
        return ResponseEntity.ok(bookingService.getPendingBookingsFlat());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        try { return ResponseEntity.ok(bookingService.confirmBooking(id)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id) {
        try { return ResponseEntity.ok(bookingService.rejectBooking(id)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    /** Legacy: redirect old schedule-request flow to new booking endpoint */
    @PostMapping("/request")
    public ResponseEntity<?> legacyRequest(@RequestBody Map<String, Object> body) {
        // Parse and forward to booking service
        try {
            Long visitorId  = Long.valueOf(body.get("visitorId").toString());
            Long exhibitId  = Long.valueOf(body.get("exhibitId").toString());
            String dateStr  = body.getOrDefault("visitDate", "").toString();
            String timeStr  = body.getOrDefault("visitTime",
                              body.getOrDefault("startTime", "10:00")).toString();
            int numberOfTickets = body.get("numberOfTickets") != null ? 
                    Integer.parseInt(body.get("numberOfTickets").toString()) : 1;

            java.time.LocalDate visitDate;
            try { visitDate = java.time.LocalDate.parse(dateStr); }
            catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", "Invalid visitDate")); }

            java.time.LocalTime visitTime;
            try { visitTime = java.time.LocalTime.parse(timeStr); }
            catch (Exception e) { visitTime = java.time.LocalTime.of(10, 0); }

            Map<String, Object> result = bookingService.createBooking(
                    visitorId, exhibitId, visitDate, visitTime,
                    com.museum.model.TicketType.ADULT, numberOfTickets);
            return org.springframework.http.ResponseEntity.status(201).body(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
