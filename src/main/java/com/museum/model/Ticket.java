package com.museum.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * A Ticket is generated when a booking is created (status: PENDING).
 * It is confirmed when admin approves the booking.
 *
 * paymentStatus: PENDING → PAID → REFUNDED
 */
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false, updatable = false)
    private LocalDateTime purchaseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketType ticketType;

    // PENDING → PAID → REFUNDED
    @Column(nullable = false)
    private String paymentStatus = "PENDING";

    // Denormalized for easy display (avoids extra joins)
    private String assignedGuideName;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @PrePersist
    protected void prePersist() { this.purchaseDate = LocalDateTime.now(); }

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }
    public TicketType getTicketType() { return ticketType; }
    public void setTicketType(TicketType ticketType) { this.ticketType = ticketType; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getAssignedGuideName() { return assignedGuideName; }
    public void setAssignedGuideName(String assignedGuideName) { this.assignedGuideName = assignedGuideName; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
}
