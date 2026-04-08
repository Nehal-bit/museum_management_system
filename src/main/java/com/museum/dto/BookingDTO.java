package com.museum.dto;

import com.museum.model.TicketType;
import jakarta.validation.constraints.Min;

public class BookingDTO {
    private Long visitorId;
    private Long scheduleId;
    private TicketType ticketType;

    /** Number of tickets to book. Must be at least 1. */
    @Min(value = 1, message = "Number of tickets must be at least 1")
    private int numberOfTickets = 1;

    public Long getVisitorId() { return visitorId; }
    public void setVisitorId(Long v) { this.visitorId = v; }
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long v) { this.scheduleId = v; }
    public TicketType getTicketType() { return ticketType; }
    public void setTicketType(TicketType v) { this.ticketType = v; }
    public int getNumberOfTickets() { return numberOfTickets; }
    public void setNumberOfTickets(int v) { this.numberOfTickets = v; }
}
