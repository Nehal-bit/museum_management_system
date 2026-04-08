package com.museum.service;

import com.museum.model.Report;
import com.museum.model.Ticket;
import com.museum.model.Exhibit;
import com.museum.repository.AdminRepository;
import com.museum.repository.ReportRepository;
import com.museum.repository.TicketRepository;
import com.museum.repository.ExhibitRepository;
import com.museum.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final AdminRepository adminRepository;
    private final AnalyticsService analyticsService;
    private final TicketRepository ticketRepository;
    private final ExhibitRepository exhibitRepository;
    private final BookingRepository bookingRepository;

    public ReportService(ReportRepository reportRepository, AdminRepository adminRepository,
                         AnalyticsService analyticsService, TicketRepository ticketRepository,
                         ExhibitRepository exhibitRepository, BookingRepository bookingRepository) {
        this.reportRepository = reportRepository;
        this.adminRepository = adminRepository;
        this.analyticsService = analyticsService;
        this.ticketRepository = ticketRepository;
        this.exhibitRepository = exhibitRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public Map<String, Object> generateReport(String reportType, Long adminId) {
        com.museum.model.Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found: " + adminId));

        String summary;
        Map<String, Object> details = new LinkedHashMap<>();

        switch (reportType.toUpperCase()) {
            case "TICKET" -> {                List<Ticket> tickets = ticketRepository.findAll();
                long paid     = tickets.stream().filter(t -> "PAID".equals(t.getPaymentStatus())).count();
                long pending  = tickets.stream().filter(t -> "PENDING".equals(t.getPaymentStatus())).count();
                long refunded = tickets.stream().filter(t -> "REFUNDED".equals(t.getPaymentStatus())).count();
                double revenue = tickets.stream().filter(t -> "PAID".equals(t.getPaymentStatus()))
                                        .mapToDouble(Ticket::getPrice).sum();
                summary = "Total Tickets: " + tickets.size() + " | Paid: " + paid +
                          " | Pending: " + pending + " | Refunded: " + refunded +
                          " | Revenue: ₹" + String.format("%.2f", revenue);
                details.put("totalTickets", tickets.size());
                details.put("paid", paid); details.put("pending", pending); details.put("refunded", refunded);
                details.put("totalRevenue", revenue);
                // breakdown by ticket type
                Map<String,Long> byType = tickets.stream()
                        .collect(Collectors.groupingBy(t -> t.getTicketType() != null ? t.getTicketType().name() : "UNKNOWN", Collectors.counting()));
                details.put("byType", byType);
            }
            case "EXHIBIT" -> {
                List<Exhibit> exhibits = exhibitRepository.findAll();
                long active   = exhibits.stream().filter(e -> "ACTIVE".equals(e.getStatus())).count();
                long archived = exhibits.stream().filter(e -> "ARCHIVED".equals(e.getStatus())).count();
                summary = "Total Exhibits: " + exhibits.size() + " | Active: " + active + " | Archived: " + archived;
                details.put("totalExhibits", exhibits.size());
                details.put("active", active); details.put("archived", archived);
                Map<String,Long> byCategory = exhibits.stream()
                        .filter(e -> e.getCategory() != null)
                        .collect(Collectors.groupingBy(Exhibit::getCategory, Collectors.counting()));
                details.put("byCategory", byCategory);
                List<Map<String,Object>> exhibitList = exhibits.stream().map(e -> {
                    Map<String,Object> em = new LinkedHashMap<>();
                    em.put("name", e.getName()); em.put("category", e.getCategory());
                    em.put("status", e.getStatus());
                    return em;
                }).toList();
                details.put("exhibits", exhibitList);
            }
            case "ANALYTICS" -> {
                var a = analyticsService.getDashboardAnalytics();
                summary = "Visitors: " + a.getTotalVisitors() + " | Bookings: " + a.getTotalBookings()
                        + " | Revenue: ₹" + String.format("%.2f", a.getTotalRevenue())
                        + " | Avg Rating: " + String.format("%.1f", a.getAverageFeedbackRating());
                details.put("totalVisitors",   a.getTotalVisitors());
                details.put("totalExhibits",   a.getTotalExhibits());
                details.put("totalBookings",   a.getTotalBookings());
                details.put("totalTickets",    a.getTotalTickets());
                details.put("totalRevenue",    a.getTotalRevenue());
                details.put("pendingBookings", a.getPendingSchedules());
                details.put("activeExhibits",  a.getActiveExhibits());
                details.put("avgRating",       a.getAverageFeedbackRating());
                details.put("bookingsByStatus",    a.getBookingsByStatus());
                details.put("ticketsByType",        a.getTicketsByType());
                details.put("exhibitsByCategory",   a.getExhibitsByCategory());
            }
            case "COMBINED" -> {
                List<Exhibit> exhibits = exhibitRepository.findAll();
                List<Ticket> tickets = ticketRepository.findAll();
                var allBookings = bookingRepository.findAll();
                double revenue = tickets.stream()
                        .filter(t -> "PAID".equals(t.getPaymentStatus()))
                        .mapToDouble(Ticket::getPrice).sum();
                long totalBookings = allBookings.size();

                List<Map<String,Object>> exhibitBreakdown = exhibits.stream().map(ex -> {
                    long exBookings = allBookings.stream()
                            .filter(b -> b.getSchedule() != null
                                    && b.getSchedule().getExhibit() != null
                                    && ex.getExhibitId().equals(b.getSchedule().getExhibit().getExhibitId()))
                            .count();
                    double exRevenue = tickets.stream()
                            .filter(t -> "PAID".equals(t.getPaymentStatus())
                                    && t.getBooking() != null
                                    && t.getBooking().getSchedule() != null
                                    && t.getBooking().getSchedule().getExhibit() != null
                                    && ex.getExhibitId().equals(t.getBooking().getSchedule().getExhibit().getExhibitId()))
                            .mapToDouble(Ticket::getPrice).sum();
                    // count distinct guide assignments for this exhibit
                    long assignedGuides = allBookings.stream()
                            .filter(b -> b.getSchedule() != null
                                    && b.getSchedule().getExhibit() != null
                                    && ex.getExhibitId().equals(b.getSchedule().getExhibit().getExhibitId()))
                            .flatMap(b -> b.getGuideAssignments().stream())
                            .filter(a -> "CONFIRMED".equals(a.getStatus()))
                            .map(a -> a.getGuide().getUserId())
                            .distinct().count();
                    Map<String,Object> em = new LinkedHashMap<>();
                    em.put("exhibitName",    ex.getName());
                    em.put("category",       ex.getCategory() != null ? ex.getCategory() : "");
                    em.put("status",         ex.getStatus());
                    em.put("totalBookings",  exBookings);
                    em.put("revenue",        exRevenue);
                    em.put("assignedGuides", assignedGuides);
                    return em;
                }).toList();

                summary = "Exhibits: " + exhibits.size() + " | Total Bookings: " + totalBookings
                        + " | Total Revenue: ₹" + String.format("%.2f", revenue);
                details.put("totalExhibits",  exhibits.size());
                details.put("totalBookings",  totalBookings);
                details.put("totalTickets",   tickets.size());
                details.put("totalRevenue",   revenue);
                details.put("exhibitBreakdown", exhibitBreakdown);
            }
            default -> throw new RuntimeException("Unknown report type: " + reportType +
                    ". Valid types: TICKET, EXHIBIT, ANALYTICS, COMBINED");
        }

        // Serialize details to JSON string for storage
        String dataJson = mapToJson(details);

        Report r = new Report();
        r.setReportType(reportType.toUpperCase());
        r.setReportData(dataJson);
        r.setGeneratedBy(admin);
        r = reportRepository.save(r);

        // Return flat response (no lazy-loaded admin entity)
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("reportId",   r.getReportId());
        response.put("reportType", r.getReportType());
        response.put("generatedOn",r.getGeneratedOn() != null ? r.getGeneratedOn().toString() : "");
        response.put("summary",    summary);
        response.put("details",    details);
        return response;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllReportsFlat() {
        return reportRepository.findAll().stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("reportId",   r.getReportId());
            m.put("reportType", r.getReportType());
            m.put("generatedOn",r.getGeneratedOn() != null ? r.getGeneratedOn().toString() : "");
            m.put("reportData", r.getReportData() != null ? r.getReportData() : "{}");
            return m;
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getReportsByTypeFlat(String type) {
        return reportRepository.findByReportType(type.toUpperCase()).stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("reportId",   r.getReportId());
            m.put("reportType", r.getReportType());
            m.put("generatedOn",r.getGeneratedOn() != null ? r.getGeneratedOn().toString() : "");
            m.put("reportData", r.getReportData() != null ? r.getReportData() : "{}");
            return m;
        }).toList();
    }

    @Transactional
    public void deleteReport(Long reportId) {
        if (!reportRepository.existsById(reportId))
            throw new RuntimeException("Report not found: " + reportId);
        reportRepository.deleteById(reportId);
    }

    /** Simple map→JSON serializer to avoid Jackson dependency in service */
    @SuppressWarnings("unchecked")
    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) sb.append(","); first = false;
            sb.append("\"").append(e.getKey()).append("\":");
            Object v = e.getValue();
            if (v == null)                    sb.append("null");
            else if (v instanceof Number)     sb.append(v);
            else if (v instanceof Boolean)    sb.append(v);
            else if (v instanceof Map)        sb.append(mapToJson((Map<String,Object>) v));
            else if (v instanceof List)       sb.append(listToJson((List<?>) v));
            else sb.append("\"").append(v.toString().replace("\"","\\\"")).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String listToJson(List<?> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object item : list) {
            if (!first) sb.append(","); first = false;
            if (item instanceof Map) sb.append(mapToJson((Map<String,Object>) item));
            else if (item instanceof Number || item instanceof Boolean) sb.append(item);
            else sb.append("\"").append(item.toString().replace("\"","\\\"")).append("\"");
        }
        return sb.append("]").toString();
    }
}
