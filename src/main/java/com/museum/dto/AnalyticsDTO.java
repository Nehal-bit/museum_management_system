package com.museum.dto;

import java.util.Map;

public class AnalyticsDTO {

    private long totalVisitors;
    private long totalExhibits;
    private long totalBookings;
    private long totalTickets;
    private double totalRevenue;
    private long pendingSchedules;
    private long activeExhibits;
    private long availableGuides;
    private double averageFeedbackRating;
    private Map<String, Long> bookingsByStatus;
    private Map<String, Long> ticketsByType;
    private Map<String, Double> revenueByTicketType;
    private Map<String, Long> exhibitsByCategory;

    public AnalyticsDTO(long totalVisitors, long totalExhibits, long totalBookings,
                        long totalTickets, double totalRevenue, long pendingSchedules,
                        long activeExhibits, long availableGuides, double averageFeedbackRating,
                        Map<String, Long> bookingsByStatus, Map<String, Long> ticketsByType,
                        Map<String, Double> revenueByTicketType, Map<String, Long> exhibitsByCategory) {
        this.totalVisitors = totalVisitors;
        this.totalExhibits = totalExhibits;
        this.totalBookings = totalBookings;
        this.totalTickets = totalTickets;
        this.totalRevenue = totalRevenue;
        this.pendingSchedules = pendingSchedules;
        this.activeExhibits = activeExhibits;
        this.availableGuides = availableGuides;
        this.averageFeedbackRating = averageFeedbackRating;
        this.bookingsByStatus = bookingsByStatus;
        this.ticketsByType = ticketsByType;
        this.revenueByTicketType = revenueByTicketType;
        this.exhibitsByCategory = exhibitsByCategory;
    }

    public long getTotalVisitors() { return totalVisitors; }
    public long getTotalExhibits() { return totalExhibits; }
    public long getTotalBookings() { return totalBookings; }
    public long getTotalTickets() { return totalTickets; }
    public double getTotalRevenue() { return totalRevenue; }
    public long getPendingSchedules() { return pendingSchedules; }
    public long getActiveExhibits() { return activeExhibits; }
    public long getAvailableGuides() { return availableGuides; }
    public double getAverageFeedbackRating() { return averageFeedbackRating; }
    public Map<String, Long> getBookingsByStatus() { return bookingsByStatus; }
    public Map<String, Long> getTicketsByType() { return ticketsByType; }
    public Map<String, Double> getRevenueByTicketType() { return revenueByTicketType; }
    public Map<String, Long> getExhibitsByCategory() { return exhibitsByCategory; }
}
