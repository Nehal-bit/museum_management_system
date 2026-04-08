package com.museum.controller;

import com.museum.dto.FeedbackDTO;
import com.museum.model.Feedback;
import com.museum.service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping({"", "/"})
    public ResponseEntity<?> submitFeedback(@RequestBody FeedbackDTO dto) {
        try {
            Feedback feedback = feedbackService.submitFeedback(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Feedback submitted. Thank you!",
                    "feedbackId", feedback.getFeedbackId(),
                    "rating", feedback.getRating()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping({"", "/"})
    public ResponseEntity<List<Feedback>> getAllFeedback() {
        return ResponseEntity.ok(feedbackService.getAllFeedback());
    }

    @GetMapping("/exhibit/{exhibitId}")
    public ResponseEntity<List<Feedback>> getFeedbackByExhibit(@PathVariable Long exhibitId) {
        return ResponseEntity.ok(feedbackService.getFeedbackByExhibit(exhibitId));
    }

    @GetMapping("/visitor/{visitorId}")
    public ResponseEntity<?> getFeedbackByVisitor(@PathVariable Long visitorId) {
        List<Feedback> list = feedbackService.getFeedbackByVisitor(visitorId);
        // Return flat maps for frontend compatibility
        List<Map<String, Object>> result = list.stream().map(f -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("feedbackId",   f.getFeedbackId());
            m.put("rating",       f.getRating());
            m.put("comments",     f.getComments());
            m.put("feedbackDate", f.getFeedbackDate() != null ? f.getFeedbackDate().toString() : "");
            m.put("exhibitName",  f.getExhibit() != null ? f.getExhibit().getName() : "N/A");
            m.put("exhibitId",    f.getExhibit() != null ? f.getExhibit().getExhibitId() : 0);
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/exhibit/{exhibitId}/rating")
    public ResponseEntity<Map<String, Object>> getAverageRating(@PathVariable Long exhibitId) {
        double avg = feedbackService.getAverageRatingForExhibit(exhibitId);
        return ResponseEntity.ok(Map.of("exhibitId", exhibitId, "averageRating", avg));
    }
}
