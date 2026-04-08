package com.museum.repository;

import com.museum.model.Schedule;
import com.museum.model.ScheduleStatus;
import com.museum.model.Visitor;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByStatus(ScheduleStatus status);
    List<Schedule> findByExhibit_ExhibitId(Long exhibitId);
    List<Schedule> findByRequestedBy(Visitor visitor);
    List<Schedule> findByVisitDate(LocalDate date);

    @Query("SELECT s FROM Schedule s WHERE s.exhibit.exhibitId = :exhibitId " +
           "AND s.visitDate = :date AND s.status = 'APPROVED'")
    List<Schedule> findAvailableSlots(@Param("exhibitId") Long exhibitId,
                                      @Param("date") LocalDate date);

    /**
     * Fetch a schedule with a PESSIMISTIC_WRITE lock so concurrent booking
     * requests are serialised at the DB level, preventing overbooking races.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Schedule s WHERE s.scheduleId = :id")
    Optional<Schedule> findByIdWithLock(@Param("id") Long id);
}
