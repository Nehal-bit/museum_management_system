package com.museum.repository;

import com.museum.model.GuideAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GuideAssignmentRepository extends JpaRepository<GuideAssignment, Long> {

    List<GuideAssignment> findByGuide_UserId(Long guideId);

    List<GuideAssignment> findByBooking_BookingId(Long bookingId);

    List<GuideAssignment> findByStatus(String status);

    Optional<GuideAssignment> findByBooking_BookingIdAndStatus(Long bookingId, String status);

    boolean existsByGuide_UserIdAndBooking_BookingId(Long guideId, Long bookingId);
}
