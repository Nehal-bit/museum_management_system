package com.museum.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A Booking represents a visitor's visit request.
 *
 * Status lifecycle:
 *   PENDING   → visitor submitted, awaiting admin approval
 *   CONFIRMED → admin approved; ticket is valid
 *   ASSIGNED  → admin has assigned a guide to this booking
 *   CANCELLED → visitor or admin cancelled
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime bookingDate;

    @Column(nullable = false)
    private String status = "PENDING";

    /** Number of tickets requested in this booking. Must be >= 1. */
    @Column(nullable = false)
    private int numberOfTickets = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visitor_id", nullable = false)
    private Visitor visitor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @JsonIgnore
    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Ticket ticket;

    @JsonIgnore
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GuideAssignment> guideAssignments = new ArrayList<>();

    @PrePersist
    protected void prePersist() { this.bookingDate = LocalDateTime.now(); }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getNumberOfTickets() { return numberOfTickets; }
    public void setNumberOfTickets(int numberOfTickets) { this.numberOfTickets = numberOfTickets; }
    public Visitor getVisitor() { return visitor; }
    public void setVisitor(Visitor visitor) { this.visitor = visitor; }
    public Schedule getSchedule() { return schedule; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }
    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }
    public List<GuideAssignment> getGuideAssignments() { return guideAssignments; }
    public void setGuideAssignments(List<GuideAssignment> guideAssignments) { this.guideAssignments = guideAssignments; }
}
