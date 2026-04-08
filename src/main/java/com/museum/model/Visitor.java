package com.museum.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "visitors")
@PrimaryKeyJoinColumn(name = "user_id")
public class Visitor extends User {

    private String phone;
    private LocalDateTime registeredOn;

    @JsonIgnore
    @OneToMany(mappedBy = "visitor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "visitor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Feedback> feedbacks = new ArrayList<>();

    @PrePersist
    protected void prePersist() {
        this.registeredOn = LocalDateTime.now();
        this.setRole(UserRole.VISITOR);
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDateTime getRegisteredOn() { return registeredOn; }
    public void setRegisteredOn(LocalDateTime registeredOn) { this.registeredOn = registeredOn; }
    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
    public List<Feedback> getFeedbacks() { return feedbacks; }
    public void setFeedbacks(List<Feedback> feedbacks) { this.feedbacks = feedbacks; }
}
