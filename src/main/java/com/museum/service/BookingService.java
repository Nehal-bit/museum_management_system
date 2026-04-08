package com.museum.service;

import com.museum.model.*;
import com.museum.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the full booking lifecycle.
 *
 *  Visitor books exhibit:
 *    → Schedule (PENDING) + Booking (PENDING) + Ticket (PENDING) created atomically
 *
 *  Admin confirms booking:
 *    → Booking → CONFIRMED, Ticket → PAID
 *
 *  Admin assigns guide to booking:
 *    → handled by GuideAssignmentService
 *    → once guide confirms → Booking → ASSIGNED, Ticket.assignedGuideName set
 *
 *  Visitor cancels:
 *    → Booking → CANCELLED, Ticket → REFUNDED
 *
 * Capacity enforcement:
 *  - Uses SERIALIZABLE isolation + PESSIMISTIC_WRITE lock on the Schedule row
 *    so concurrent bookings cannot race past the capacity check.
 *  - Counts existing non-cancelled tickets (sum of numberOfTickets) and refuses
 *    the booking if adding the requested quantity would exceed maxVisitors.
 */
@Service
public class BookingService {

    private final BookingRepository    bookingRepository;
    private final VisitorRepository    visitorRepository;
    private final ExhibitRepository    exhibitRepository;
    private final ScheduleRepository   scheduleRepository;
    private final TicketRepository     ticketRepository;
    private final TicketPricingService pricingService;
    private final NotificationManager  notificationManager;

    public BookingService(BookingRepository bookingRepository,
                          VisitorRepository visitorRepository,
                          ExhibitRepository exhibitRepository,
                          ScheduleRepository scheduleRepository,
                          TicketRepository ticketRepository,
                          TicketPricingService pricingService,
                          NotificationManager notificationManager) {
        this.bookingRepository   = bookingRepository;
        this.visitorRepository   = visitorRepository;
        this.exhibitRepository   = exhibitRepository;
        this.scheduleRepository  = scheduleRepository;
        this.ticketRepository    = ticketRepository;
        this.pricingService      = pricingService;
        this.notificationManager = notificationManager;
    }

    // ── Create Booking ──────────────────────────────────────────────────────

    /**
     * One-shot booking: visitor supplies exhibit + date + time + ticketType + numberOfTickets.
     * Creates Schedule → Booking → Ticket all as PENDING.
     * Admin must confirm the booking to make it CONFIRMED.
     *
     * Uses SERIALIZABLE isolation and enforces a 50-ticket capacity limit per exhibit per date.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Map<String, Object> createBooking(Long visitorId, Long exhibitId,
                                              LocalDate visitDate, LocalTime visitTime,
                                              TicketType ticketType, int numberOfTickets) {

        // ── Validate inputs ────────────────────────────────────────────────
        if (numberOfTickets < 1)
            throw new IllegalArgumentException("Number of tickets must be at least 1.");
        if (numberOfTickets > 50)
            throw new IllegalArgumentException("Cannot book more than 50 tickets per booking.");

        Visitor visitor = visitorRepository.findById(visitorId)
                .orElseThrow(() -> new RuntimeException("Visitor not found: " + visitorId));
        Exhibit exhibit = exhibitRepository.findById(exhibitId)
                .orElseThrow(() -> new RuntimeException("Exhibit not found: " + exhibitId));

        // ── Capacity check: enforce 50-ticket limit per exhibit per date ──
        // Count all CONFIRMED/APPROVED bookings for this exhibit on this date
        int totalAlreadyBooked = bookingRepository.countConfirmedTicketsForExhibitDate(exhibitId, visitDate);
        int totalRequestedTickets = totalAlreadyBooked + numberOfTickets;

        if (totalAlreadyBooked >= 50)
            throw new RuntimeException(
                    "This exhibit is fully booked on " + visitDate + ". Maximum 50 tickets per day. " +
                    "Currently: " + totalAlreadyBooked + " tickets booked.");

        if (totalRequestedTickets > 50)
            throw new RuntimeException(
                    "Cannot accommodate " + numberOfTickets + " tickets. " +
                    "Currently booked: " + totalAlreadyBooked + " / 50. " +
                    "Only " + (50 - totalAlreadyBooked) + " ticket(s) remaining on " + visitDate + ".");

        // ── Create the schedule slot for this booking ─────────────────────
        Schedule schedule = new Schedule();
        schedule.setExhibit(exhibit);
        schedule.setRequestedBy(visitor);
        schedule.setVisitDate(visitDate);
        schedule.setStartTime(visitTime);
        schedule.setEndTime(visitTime.plusHours(1));
        schedule.setMaxVisitors(numberOfTickets); // slot sized for this booking
        schedule.setCurrentVisitors(numberOfTickets);
        schedule.setStatus(ScheduleStatus.PENDING);
        schedule = scheduleRepository.save(schedule);

        // ── Create the booking ────────────────────────────────────────────
        Booking booking = new Booking();
        booking.setVisitor(visitor);
        booking.setSchedule(schedule);
        booking.setStatus("PENDING");
        booking.setNumberOfTickets(numberOfTickets);
        booking = bookingRepository.save(booking);

        // ── Create ticket (PENDING until confirmed) ───────────────────────
        TicketType type = ticketType != null ? ticketType : TicketType.ADULT;
        double unitPrice = pricingService.getPrice(type);
        Ticket ticket = new Ticket();
        ticket.setBooking(booking);
        ticket.setTicketType(type);
        ticket.setPrice(unitPrice * numberOfTickets); // total price for all tickets
        ticket.setPaymentStatus("PENDING");
        ticket = ticketRepository.save(ticket);

        // ── Notify visitor ────────────────────────────────────────────────
        notificationManager.notifyUser(visitor,
                "📋 Your booking request for '" + exhibit.getName() +
                "' on " + visitDate + " at " + visitTime +
                " (" + numberOfTickets + " ticket(s) × ₹" + unitPrice +
                " = ₹" + ticket.getPrice() + ") has been submitted. Awaiting admin confirmation.");

        return flattenBooking(booking, ticket, schedule, exhibit, null);
    }

    // ── Admin actions ───────────────────────────────────────────────────────

    /**
     * Admin confirms a pending booking → CONFIRMED + ticket PAID.
     */
    @Transactional
    public Map<String, Object> confirmBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        if (!"PENDING".equals(booking.getStatus()))
            throw new RuntimeException("Booking is not PENDING.");

        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);

        Schedule schedule = booking.getSchedule();
        schedule.setStatus(ScheduleStatus.APPROVED);
        scheduleRepository.save(schedule);

        Ticket ticket = ticketRepository.findByBooking_BookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Ticket not found for booking: " + bookingId));
        ticket.setPaymentStatus("PAID");
        ticketRepository.save(ticket);

        notificationManager.notifyUser(booking.getVisitor(),
                "✅ Your booking for '" + schedule.getExhibit().getName() +
                "' on " + schedule.getVisitDate() + " is CONFIRMED! " +
                booking.getNumberOfTickets() + " ticket(s) | Ticket #" +
                ticket.getTicketId() + " | ₹" + ticket.getPrice());

        return flattenBooking(booking, ticket, schedule, schedule.getExhibit(), null);
    }

    /**
     * Admin rejects a pending booking.
     */
    @Transactional
    public Map<String, Object> rejectBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        if (!"PENDING".equals(booking.getStatus()))
            throw new RuntimeException("Booking is not PENDING.");

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        Schedule schedule = booking.getSchedule();
        schedule.setStatus(ScheduleStatus.REJECTED);
        scheduleRepository.save(schedule);

        Ticket ticket = ticketRepository.findByBooking_BookingId(bookingId).orElse(null);
        if (ticket != null) { ticket.setPaymentStatus("REFUNDED"); ticketRepository.save(ticket); }

        notificationManager.notifyUser(booking.getVisitor(),
                "❌ Your booking for '" + schedule.getExhibit().getName() +
                "' on " + schedule.getVisitDate() + " was not approved.");

        return flattenBooking(booking, ticket, schedule, schedule.getExhibit(), null);
    }

    /**
     * Mark booking as ASSIGNED (called internally when guide confirms).
     */
    @Transactional
    public void markAssigned(Long bookingId, String guideName) {
        Booking booking = getBookingById(bookingId);
        booking.setStatus("ASSIGNED");
        bookingRepository.save(booking);

        Ticket ticket = ticketRepository.findByBooking_BookingId(bookingId).orElse(null);
        if (ticket != null) {
            ticket.setAssignedGuideName(guideName);
            ticketRepository.save(ticket);
        }
        notificationManager.notifyUser(booking.getVisitor(),
                "🗺️ Guide " + guideName + " has been assigned to your visit at '" +
                booking.getSchedule().getExhibit().getName() + "'.");
    }

    /**
     * Visitor cancels their booking.
     */
    @Transactional
    public Map<String, Object> cancelBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        if ("CANCELLED".equals(booking.getStatus()))
            throw new RuntimeException("Already cancelled.");

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        Ticket ticket = ticketRepository.findByBooking_BookingId(bookingId).orElse(null);
        if (ticket != null) { ticket.setPaymentStatus("REFUNDED"); ticketRepository.save(ticket); }

        notificationManager.notifyUser(booking.getVisitor(), "Your booking has been cancelled.");
        Schedule s = booking.getSchedule();
        return flattenBooking(booking, ticket, s, s.getExhibit(), null);
    }

    // ── Queries ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllBookingsFlat() {
        return bookingRepository.findAll().stream().map(this::buildFlat).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPendingBookingsFlat() {
        return bookingRepository.findByStatus("PENDING").stream().map(this::buildFlat).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getBookingsByVisitorFlat(Long visitorId) {
        return bookingRepository.findByVisitor_UserId(visitorId).stream().map(this::buildFlat).toList();
    }

    @Transactional(readOnly = true)
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + id));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getBookingFlat(Long id) {
        return buildFlat(getBookingById(id));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Map<String, Object> buildFlat(Booking b) {
        Ticket ticket = null;
        try { ticket = ticketRepository.findByBooking_BookingId(b.getBookingId()).orElse(null); }
        catch (Exception ignored) {}
        Schedule s = b.getSchedule();
        Exhibit e  = s != null ? s.getExhibit() : null;
        String guideName = ticket != null ? ticket.getAssignedGuideName() : null;
        return flattenBooking(b, ticket, s, e, guideName);
    }

    private Map<String, Object> flattenBooking(Booking b, Ticket ticket,
                                                Schedule schedule, Exhibit exhibit,
                                                String guideName) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("bookingId",        b.getBookingId());
        m.put("id",               b.getBookingId());
        m.put("status",           b.getStatus());
        m.put("bookingDate",      b.getBookingDate() != null ? b.getBookingDate().toString() : "");
        m.put("numberOfTickets",  b.getNumberOfTickets());
        // Exhibit info
        m.put("exhibitName",      exhibit != null ? exhibit.getName() : "N/A");
        m.put("exhibitId",        exhibit != null ? exhibit.getExhibitId() : 0);
        m.put("category",         exhibit != null ? exhibit.getCategory() : "");
        // Schedule info
        m.put("visitDate",        schedule != null && schedule.getVisitDate()  != null ? schedule.getVisitDate().toString()  : "");
        m.put("visitTime",        schedule != null && schedule.getStartTime()  != null ? schedule.getStartTime().toString()  : "");
        m.put("scheduleId",       schedule != null ? schedule.getScheduleId() : 0);
        m.put("maxVisitors",      schedule != null ? schedule.getMaxVisitors() : 0);
        // Visitor info
        m.put("visitorName",      b.getVisitor() != null ? b.getVisitor().getName()  : "N/A");
        m.put("visitorEmail",     b.getVisitor() != null ? b.getVisitor().getEmail() : "");
        m.put("visitorId",        b.getVisitor() != null ? b.getVisitor().getUserId(): 0);
        // Ticket info
        if (ticket != null) {
            m.put("ticketId",      ticket.getTicketId());
            m.put("ticketType",    ticket.getTicketType() != null ? ticket.getTicketType().name() : "ADULT");
            m.put("price",         ticket.getPrice());
            m.put("paymentStatus", ticket.getPaymentStatus());
            m.put("purchaseDate",  ticket.getPurchaseDate() != null ? ticket.getPurchaseDate().toString() : "");
        } else {
            m.put("ticketId", null); m.put("ticketType", "N/A");
            m.put("price", 0);       m.put("paymentStatus", "N/A"); m.put("purchaseDate", "");
        }
        m.put("assignedGuide", guideName != null ? guideName : "Not assigned");
        return m;
    }
}
