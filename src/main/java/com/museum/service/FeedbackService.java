package com.museum.service;

import com.museum.dto.FeedbackDTO;
import com.museum.model.*;
import com.museum.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final VisitorRepository visitorRepository;
    private final ExhibitRepository exhibitRepository;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           VisitorRepository visitorRepository,
                           ExhibitRepository exhibitRepository) {
        this.feedbackRepository = feedbackRepository;
        this.visitorRepository = visitorRepository;
        this.exhibitRepository = exhibitRepository;
    }

    @Transactional
    public Feedback submitFeedback(FeedbackDTO dto) {
        Visitor visitor = visitorRepository.findById(dto.getVisitorId())
                .orElseThrow(() -> new RuntimeException("Visitor not found: " + dto.getVisitorId()));
        Exhibit exhibit = exhibitRepository.findById(dto.getExhibitId())
                .orElseThrow(() -> new RuntimeException("Exhibit not found: " + dto.getExhibitId()));
        Feedback f = new Feedback();
        f.setVisitor(visitor); f.setExhibit(exhibit);
        f.setRating(dto.getRating()); f.setComments(dto.getComments());
        return feedbackRepository.save(f);
    }

    @Transactional(readOnly = true) public List<Feedback> getAllFeedback() { return feedbackRepository.findAll(); }
    @Transactional(readOnly = true) public List<Feedback> getFeedbackByExhibit(Long exhibitId) { return feedbackRepository.findByExhibit_ExhibitId(exhibitId); }
    @Transactional(readOnly = true) public List<Feedback> getFeedbackByVisitor(Long visitorId) { return feedbackRepository.findByVisitor_UserId(visitorId); }
    @Transactional(readOnly = true) public double getAverageRatingForExhibit(Long exhibitId) {
        Double avg = feedbackRepository.averageRatingByExhibit(exhibitId); return avg != null ? avg : 0.0;
    }
}
