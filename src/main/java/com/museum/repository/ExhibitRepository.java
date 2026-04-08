package com.museum.repository;

import com.museum.model.Exhibit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExhibitRepository extends JpaRepository<Exhibit, Long> {

    List<Exhibit> findByStatus(String status);

    List<Exhibit> findByCategory(String category);

    List<Exhibit> findByNameContainingIgnoreCase(String name);
}
