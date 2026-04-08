package com.museum.repository;

import com.museum.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByVisitor_UserId(Long visitorId);

    List<Feedback> findByExhibit_ExhibitId(Long exhibitId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.exhibit.exhibitId = :exhibitId")
    Double averageRatingByExhibit(@Param("exhibitId") Long exhibitId);
}
