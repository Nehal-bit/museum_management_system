package com.museum.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    @Column(nullable = false)
    private LocalDate visitDate;

    private LocalTime startTime;
    private LocalTime endTime;

    @Column(nullable = false)
    private int maxVisitors = 30;

    @Column(nullable = false)
    private int currentVisitors = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleStatus status = ScheduleStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exhibit_id", nullable = false)
    private Exhibit exhibit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by")
    private Visitor requestedBy;

    @JsonIgnore
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    public boolean hasAvailableSlots() { return currentVisitors < maxVisitors; }
    public int availableSlots() { return maxVisitors - currentVisitors; }

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public LocalDate getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public int getMaxVisitors() { return maxVisitors; }
    public void setMaxVisitors(int maxVisitors) { this.maxVisitors = maxVisitors; }
    public int getCurrentVisitors() { return currentVisitors; }
    public void setCurrentVisitors(int currentVisitors) { this.currentVisitors = currentVisitors; }
    public ScheduleStatus getStatus() { return status; }
    public void setStatus(ScheduleStatus status) { this.status = status; }
    public Exhibit getExhibit() { return exhibit; }
    public void setExhibit(Exhibit exhibit) { this.exhibit = exhibit; }
    public Visitor getRequestedBy() { return requestedBy; }
    public void setRequestedBy(Visitor requestedBy) { this.requestedBy = requestedBy; }
    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
}
