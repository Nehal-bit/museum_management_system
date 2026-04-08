package com.museum.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public class ScheduleRequestDTO {

    @NotNull(message = "Exhibit ID is required")
    private Long exhibitId;

    @NotNull(message = "Visitor ID is required")
    private Long visitorId;

    @NotNull(message = "Visit date is required")
    private LocalDate visitDate;

    private LocalTime startTime;
    private LocalTime endTime;
    private String visitTime;  // frontend sends HH:mm string
    private int maxVisitors = 30;

    public LocalTime getResolvedStartTime() {
        if (startTime != null) return startTime;
        if (visitTime != null && !visitTime.isBlank()) {
            try { return LocalTime.parse(visitTime); } catch (Exception ignored) {}
        }
        return LocalTime.of(10, 0);
    }
    public LocalTime getResolvedEndTime() {
        if (endTime != null) return endTime;
        return getResolvedStartTime().plusHours(1);
    }

    public Long getExhibitId() { return exhibitId; }
    public void setExhibitId(Long exhibitId) { this.exhibitId = exhibitId; }
    public Long getVisitorId() { return visitorId; }
    public void setVisitorId(Long visitorId) { this.visitorId = visitorId; }
    public LocalDate getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getVisitTime() { return visitTime; }
    public void setVisitTime(String visitTime) { this.visitTime = visitTime; }
    public int getMaxVisitors() { return maxVisitors; }
    public void setMaxVisitors(int maxVisitors) { this.maxVisitors = maxVisitors; }
}
