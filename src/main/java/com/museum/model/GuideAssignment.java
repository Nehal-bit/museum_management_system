package com.museum.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Records an admin assigning a Guide to a specific Booking (visit request).
 * A guide is assigned to a BOOKING, not an exhibit — they escort that
 * particular visitor group on that specific date and time.
 */
@Entity
@Table(name = "guide_assignments")
public class GuideAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignmentId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime assignedDate;

    // PENDING → guide notified
    // CONFIRMED → guide accepted
    // REJECTED → guide declined
    @Column(nullable = false)
    private String status = "PENDING";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id", nullable = false)
    private Guide guide;

    /** The booking (visit request) this guide is assigned to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exhibit_id", nullable = false)
    private Exhibit exhibit;

    @PrePersist
    protected void prePersist() { this.assignedDate = LocalDateTime.now(); }

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
    public LocalDateTime getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDateTime assignedDate) { this.assignedDate = assignedDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Guide getGuide() { return guide; }
    public void setGuide(Guide guide) { this.guide = guide; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public Exhibit getExhibit() { return exhibit; }
    public void setExhibit(Exhibit exhibit) { this.exhibit = exhibit; }
}
