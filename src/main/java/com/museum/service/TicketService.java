package com.museum.service;

import com.museum.model.Ticket;
import com.museum.model.TicketType;
import com.museum.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    public TicketService(TicketRepository ticketRepository) { this.ticketRepository = ticketRepository; }

    @Transactional(readOnly = true) public List<Ticket> getAllTickets() { return ticketRepository.findAll(); }
    @Transactional(readOnly = true) public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id).orElseThrow(() -> new RuntimeException("Ticket not found: " + id));
    }
    @Transactional(readOnly = true) public Ticket getTicketByBookingId(Long bookingId) {
        return ticketRepository.findByBooking_BookingId(bookingId).orElseThrow(() -> new RuntimeException("Ticket not found for booking: " + bookingId));
    }
    @Transactional(readOnly = true) public List<Ticket> getTicketsByType(TicketType type) { return ticketRepository.findByTicketType(type); }
    @Transactional(readOnly = true) public double getTotalRevenue() {
        Double r = ticketRepository.totalRevenue(); return r != null ? r : 0.0;
    }
}
