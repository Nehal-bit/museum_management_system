package com.museum.repository;

import com.museum.model.Guide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuideRepository extends JpaRepository<Guide, Long> {

    Optional<Guide> findByEmail(String email);

    List<Guide> findByAvailable(boolean available);
}
