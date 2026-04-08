package com.museum.service;

import com.museum.dto.AnalyticsDTO;
import com.museum.model.*;
import com.museum.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final VisitorRepository visitorRepository;
    private final ExhibitRepository exhibitRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final FeedbackRepository feedbackRepository;
    private final ScheduleRepository scheduleRepository;
    private final GuideRepository guideRepository;

    public AnalyticsService(VisitorRepository visitorRepository, ExhibitRepository exhibitRepository,
                            BookingRepository bookingRepository, TicketRepository ticketRepository,
                            FeedbackRepository feedbackRepository, ScheduleRepository scheduleRepository,
                            GuideRepository guideRepository) {
        this.visitorRepository = visitorRepository; this.exhibitRepository = exhibitRepository;
        this.bookingRepository = bookingRepository; this.ticketRepository = ticketRepository;
        this.feedbackRepository = feedbackRepository; this.scheduleRepository = scheduleRepository;
        this.guideRepository = guideRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsDTO getDashboardAnalytics() {
        long totalVisitors  = visitorRepository.count();
        long totalExhibits  = exhibitRepository.count();
        long totalBookings  = bookingRepository.count();
        long totalTickets   = ticketRepository.count();
        double totalRevenue = ticketRepository.totalRevenue() != null ? ticketRepository.totalRevenue() : 0.0;
        long pendingSchedules = scheduleRepository.findByStatus(ScheduleStatus.PENDING).size();
        long activeExhibits   = exhibitRepository.findByStatus("ACTIVE").size();
        long availableGuides  = guideRepository.findByAvailable(true).size();

        double avgRating = feedbackRepository.findAll().stream()
                .mapToInt(Feedback::getRating).average().orElse(0.0);

        Map<String,Long> bookingsByStatus = bookingRepository.findAll().stream()
                .collect(Collectors.groupingBy(Booking::getStatus, Collectors.counting()));

        Map<String,Long> ticketsByType = ticketRepository.findAll().stream()
                .collect(Collectors.groupingBy(t -> t.getTicketType().name(), Collectors.counting()));

        Map<String,Double> revenueByType = ticketRepository.findAll().stream()
                .collect(Collectors.groupingBy(t -> t.getTicketType().name(), Collectors.summingDouble(Ticket::getPrice)));

        Map<String,Long> exhibitsByCategory = exhibitRepository.findAll().stream()
                .filter(e -> e.getCategory() != null)
                .collect(Collectors.groupingBy(Exhibit::getCategory, Collectors.counting()));

        return new AnalyticsDTO(totalVisitors, totalExhibits, totalBookings, totalTickets, totalRevenue,
                pendingSchedules, activeExhibits, availableGuides, avgRating,
                bookingsByStatus, ticketsByType, revenueByType, exhibitsByCategory);
    }

    @Transactional(readOnly = true) public List<Ticket> getTicketReport() { return ticketRepository.findAll(); }
    @Transactional(readOnly = true) public List<Exhibit> getExhibitReport() { return exhibitRepository.findAll(); }
}
