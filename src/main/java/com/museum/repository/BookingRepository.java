package com.museum.repository;

import com.museum.model.Booking;
import com.museum.model.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByVisitor(Visitor visitor);
    List<Booking> findByVisitor_UserId(Long visitorId);
    List<Booking> findByStatus(String status);

    /** Count bookings (not cancelled) for a schedule — used for availability checks. */
    long countBySchedule_ScheduleIdAndStatusNot(Long scheduleId, String status);

    /**
     * Sum of numberOfTickets for all non-cancelled bookings on a given schedule.
     * Returns 0 if no bookings exist (COALESCE handles the null from SUM on empty set).
     */
    @Query("SELECT COALESCE(SUM(b.numberOfTickets), 0) FROM Booking b " +
           "WHERE b.schedule.scheduleId = :scheduleId AND b.status <> 'CANCELLED'")
    int sumTicketsBySchedule(@Param("scheduleId") Long scheduleId);

    /**
     * Count total confirmed tickets for an exhibit on a given date.
     * Includes only CONFIRMED and ASSIGNED bookings (excludes PENDING and CANCELLED).
     * This ensures total bookings never exceed 50 per exhibit per date.
     */
    @Query("SELECT COALESCE(SUM(b.numberOfTickets), 0) FROM Booking b " +
           "INNER JOIN b.schedule s " +
           "WHERE s.exhibit.exhibitId = :exhibitId AND s.visitDate = :visitDate " +
           "AND b.status IN ('CONFIRMED', 'ASSIGNED')")
    int countConfirmedTicketsForExhibitDate(@Param("exhibitId") Long exhibitId, @Param("visitDate") LocalDate visitDate);
}
