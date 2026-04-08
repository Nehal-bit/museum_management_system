package com.museum.controller;

import com.museum.model.TicketType;
import com.museum.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Visitor creates a booking.
     * Body: { visitorId, exhibitId, visitDate, visitTime, ticketType, numberOfTickets }
     *
     * numberOfTickets defaults to 1 if omitted.
     * Returns 400 if capacity would be exceeded or numberOfTickets < 1.
     */
    @PostMapping({"", "/"})
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> body) {
        try {
            Long visitorId = Long.valueOf(body.get("visitorId").toString());
            Long exhibitId = Long.valueOf(body.get("exhibitId").toString());
            String dateStr = body.get("visitDate").toString();
            String timeStr = body.getOrDefault("visitTime", "10:00").toString();
            String typeStr = body.getOrDefault("ticketType", "ADULT").toString();

            // Parse numberOfTickets — default 1, reject if < 1
            int numberOfTickets;
            try {
                numberOfTickets = Integer.parseInt(body.getOrDefault("numberOfTickets", "1").toString());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "numberOfTickets must be a valid integer."));
            }
            if (numberOfTickets < 1)
                return ResponseEntity.badRequest().body(Map.of("error", "Number of tickets must be at least 1."));

            LocalDate visitDate = LocalDate.parse(dateStr);
            LocalTime visitTime;
            try { visitTime = LocalTime.parse(timeStr); }
            catch (Exception e) { visitTime = LocalTime.of(10, 0); }

            TicketType ticketType;
            try { ticketType = TicketType.valueOf(typeStr.toUpperCase()); }
            catch (Exception e) { ticketType = TicketType.ADULT; }

            Map<String, Object> result = bookingService.createBooking(
                    visitorId, exhibitId, visitDate, visitTime, ticketType, numberOfTickets);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping({"", "/"})
    public ResponseEntity<?> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookingsFlat());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try { return ResponseEntity.ok(bookingService.getBookingFlat(id)); }
        catch (RuntimeException e) { return ResponseEntity.notFound().build(); }
    }

    @GetMapping("/visitor/{visitorId}")
    public ResponseEntity<?> getByVisitor(@PathVariable Long visitorId) {
        return ResponseEntity.ok(bookingService.getBookingsByVisitorFlat(visitorId));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        try { return ResponseEntity.ok(bookingService.cancelBooking(id)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }
}
