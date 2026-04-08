package com.museum.controller;

import com.museum.model.Ticket;
import com.museum.model.TicketType;
import com.museum.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping({"", "/"})
    public ResponseEntity<List<Ticket>> getAllTickets() { return ResponseEntity.ok(ticketService.getAllTickets()); }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try { return ResponseEntity.ok(ticketService.getTicketById(id)); }
        catch (RuntimeException e) { return ResponseEntity.notFound().build(); }
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getByBooking(@PathVariable Long bookingId) {
        try { return ResponseEntity.ok(ticketService.getTicketByBookingId(bookingId)); }
        catch (RuntimeException e) { return ResponseEntity.notFound().build(); }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Ticket>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(ticketService.getTicketsByType(TicketType.valueOf(type.toUpperCase())));
    }

    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenue() {
        return ResponseEntity.ok(Map.of("totalRevenue", ticketService.getTotalRevenue(), "currency", "INR"));
    }
}
