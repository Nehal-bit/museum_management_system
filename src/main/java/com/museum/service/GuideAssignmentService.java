package com.museum.service;

import com.museum.model.*;
import com.museum.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages guide assignments to BOOKINGS (not exhibits).
 *
 * Flow:
 *  1. Admin picks a confirmed booking + a guide → assignGuideToBooking()
 *  2. Guide is notified
 *  3. Guide confirms → booking status → ASSIGNED, ticket gets guide name
 *  4. Guide rejects → assignment REJECTED, admin can reassign
 */
@Service
public class GuideAssignmentService {

    private final GuideAssignmentRepository assignmentRepository;
    private final GuideRepository guideRepository;
    private final BookingService bookingService;
    private final NotificationManager notificationManager;

    public GuideAssignmentService(GuideAssignmentRepository assignmentRepository,
                                  GuideRepository guideRepository,
                                  BookingService bookingService,
                                  NotificationManager notificationManager) {
        this.assignmentRepository = assignmentRepository;
        this.guideRepository = guideRepository;
        this.bookingService = bookingService;
        this.notificationManager = notificationManager;
    }

    /**
     * Admin assigns a guide to a specific booking.
     */
    @Transactional
    public Map<String, Object> assignGuideToBooking(Long guideId, Long bookingId) {
        Guide guide = guideRepository.findById(guideId)
                .orElseThrow(() -> new RuntimeException("Guide not found: " + guideId));
        Booking booking = bookingService.getBookingById(bookingId);

        if ("CANCELLED".equals(booking.getStatus()))
            throw new RuntimeException("Cannot assign guide to a cancelled booking.");
        if ("PENDING".equals(booking.getStatus()))
            throw new RuntimeException("Booking must be confirmed before assigning a guide.");

        GuideAssignment assignment = new GuideAssignment();
        assignment.setGuide(guide);
        assignment.setBooking(booking);
        assignment.setExhibit(booking.getSchedule().getExhibit());
        assignment.setStatus("PENDING");
        assignment = assignmentRepository.save(assignment);

        // Notify guide with full booking context
        Schedule s = booking.getSchedule();
        String exhibitName = s != null && s.getExhibit() != null ? s.getExhibit().getName() : "an exhibit";
        String visitDate   = s != null && s.getVisitDate() != null ? s.getVisitDate().toString() : "TBD";
        String visitTime   = s != null && s.getStartTime() != null ? s.getStartTime().toString() : "TBD";
        String visitorName = booking.getVisitor() != null ? booking.getVisitor().getName() : "a visitor";

        notificationManager.notifyUser(guide,
                "📋 You have been assigned to guide " + visitorName +
                " at '" + exhibitName + "' on " + visitDate + " at " + visitTime +
                ". Please confirm or reject.");

        return flattenAssignment(assignment, booking);
    }

    /**
     * Guide confirms their assignment.
     */
    @Transactional
    public Map<String, Object> confirmAssignment(Long assignmentId) {
        GuideAssignment assignment = getById(assignmentId);
        if (!"PENDING".equals(assignment.getStatus()))
            throw new RuntimeException("Assignment is not PENDING.");

        assignment.setStatus("CONFIRMED");
        assignmentRepository.save(assignment);

        // Update booking and ticket
        bookingService.markAssigned(assignment.getBooking().getBookingId(),
                                    assignment.getGuide().getName());

        return flattenAssignment(assignment, assignment.getBooking());
    }

    /**
     * Guide rejects their assignment.
     */
    @Transactional
    public Map<String, Object> rejectAssignment(Long assignmentId) {
        GuideAssignment assignment = getById(assignmentId);
        if (!"PENDING".equals(assignment.getStatus()))
            throw new RuntimeException("Assignment is not PENDING.");

        assignment.setStatus("REJECTED");
        assignmentRepository.save(assignment);

        // Notify admin (via booking visitor notification for now)
        notificationManager.notifyUser(assignment.getGuide(),
                "You have rejected the assignment for booking #" +
                assignment.getBooking().getBookingId() + ".");

        return flattenAssignment(assignment, assignment.getBooking());
    }

    // ── Queries ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllAssignmentsFlat() {
        return assignmentRepository.findAll().stream()
                .map(a -> flattenAssignment(a, a.getBooking())).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAssignmentsByGuideFlat(Long guideId) {
        return assignmentRepository.findByGuide_UserId(guideId).stream()
                .map(a -> flattenAssignment(a, a.getBooking())).toList();
    }

    @Transactional(readOnly = true)
    public List<Guide> getAllGuides() { return guideRepository.findAll(); }

    @Transactional(readOnly = true)
    public GuideAssignment getById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + id));
    }

    // ── Helper ─────────────────────────────────────────────────────────────

    Map<String, Object> flattenAssignment(GuideAssignment a, Booking b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("assignmentId",  a.getAssignmentId());
        m.put("id",            a.getAssignmentId());
        m.put("status",        a.getStatus());
        m.put("assignedDate",  a.getAssignedDate() != null ? a.getAssignedDate().toString() : "");
        m.put("guideName",     a.getGuide() != null ? a.getGuide().getName() : "N/A");
        m.put("guideId",       a.getGuide() != null ? a.getGuide().getUserId() : 0);
        if (b != null) {
            m.put("bookingId",    b.getBookingId());
            m.put("bookingStatus",b.getStatus());
            m.put("visitorName",  b.getVisitor() != null ? b.getVisitor().getName() : "N/A");
            m.put("visitorEmail", b.getVisitor() != null ? b.getVisitor().getEmail() : "");
            Schedule s = b.getSchedule();
            Exhibit  e = s != null ? s.getExhibit() : null;
            m.put("exhibitName", e != null ? e.getName() : "N/A");
            m.put("visitDate",   s != null && s.getVisitDate()  != null ? s.getVisitDate().toString()  : "");
            m.put("visitTime",   s != null && s.getStartTime()  != null ? s.getStartTime().toString()  : "");
        }
        return m;
    }
}
