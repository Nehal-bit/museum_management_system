package com.museum.dto;

public class FeedbackDTO {
    private Long visitorId;
    private Long exhibitId;
    private int rating;
    private String comments;

    public Long getVisitorId() { return visitorId; }
    public void setVisitorId(Long v) { this.visitorId = v; }
    public Long getExhibitId() { return exhibitId; }
    public void setExhibitId(Long v) { this.exhibitId = v; }
    public int getRating() { return rating; }
    public void setRating(int v) { this.rating = v; }
    public String getComments() { return comments; }
    public void setComments(String v) { this.comments = v; }
}
