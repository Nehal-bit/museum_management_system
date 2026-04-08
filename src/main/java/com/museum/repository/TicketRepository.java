package com.museum.repository;

import com.museum.model.Ticket;
import com.museum.model.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByBooking_BookingId(Long bookingId);

    List<Ticket> findByTicketType(TicketType type);

    @Query("SELECT SUM(t.price) FROM Ticket t WHERE t.paymentStatus = 'PAID'")
    Double totalRevenue();
}
