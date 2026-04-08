package com.museum.dto;

import com.museum.model.TicketType;

import java.time.LocalDateTime;

/**
 * DTO for returning ticket information to the client.
 */
public class TicketDTO {

    private Long ticketId;
    private Long bookingId;
    private TicketType ticketType;
    private double price;
    private String paymentStatus;
    private LocalDateTime purchaseDate;

    // Visitor info
    private String visitorName;
    private String visitorEmail;

    // Exhibit info
    private String exhibitName;
    private String visitDate;
    private String timeSlot;

    public TicketDTO() {
    }

    public TicketDTO(Long ticketId, Long bookingId, TicketType ticketType, double price, String paymentStatus,
                     LocalDateTime purchaseDate, String visitorName, String visitorEmail, String exhibitName,
                     String visitDate, String timeSlot) {
        this.ticketId = ticketId;
        this.bookingId = bookingId;
        this.ticketType = ticketType;
        this.price = price;
        this.paymentStatus = paymentStatus;
        this.purchaseDate = purchaseDate;
        this.visitorName = visitorName;
        this.visitorEmail = visitorEmail;
        this.exhibitName = exhibitName;
        this.visitDate = visitDate;
        this.timeSlot = timeSlot;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public TicketType getTicketType() {
        return ticketType;
    }

    public void setTicketType(TicketType ticketType) {
        this.ticketType = ticketType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }

    public String getVisitorEmail() {
        return visitorEmail;
    }

    public void setVisitorEmail(String visitorEmail) {
        this.visitorEmail = visitorEmail;
    }

    public String getExhibitName() {
        return exhibitName;
    }

    public void setExhibitName(String exhibitName) {
        this.exhibitName = exhibitName;
    }

    public String getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }
}
