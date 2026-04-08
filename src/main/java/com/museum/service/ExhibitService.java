package com.museum.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.museum.model.Exhibit;
import com.museum.repository.ExhibitRepository;

@Service
public class ExhibitService {

    private final ExhibitRepository exhibitRepository;

    public ExhibitService(ExhibitRepository exhibitRepository) {
        this.exhibitRepository = exhibitRepository;
    }

    @Transactional
    public Exhibit addExhibit(Exhibit exhibit) {
        if (exhibit.getStatus() == null || exhibit.getStatus().isBlank()) {
            exhibit.setStatus("ACTIVE");
        }
        if (exhibit.getMaxOccupancy() <= 0) {
            exhibit.setMaxOccupancy(50);
        }
        return exhibitRepository.save(exhibit);
    }

    @Transactional
    public Exhibit updateExhibit(Long id, Exhibit updated) {
        Exhibit existing = getExhibitById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setCategory(updated.getCategory());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
        if (updated.getMaxOccupancy() > 0) existing.setMaxOccupancy(updated.getMaxOccupancy());
        return exhibitRepository.save(existing);
    }

    @Transactional
    public void deleteExhibit(Long id) {
        Exhibit exhibit = getExhibitById(id);
        exhibit.setStatus("ARCHIVED");
        exhibitRepository.save(exhibit);
    }

    @Transactional
    public void hardDeleteExhibit(Long id) {
        exhibitRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Exhibit> getAllExhibits() {
        return exhibitRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Exhibit> getActiveExhibits() {
        return exhibitRepository.findByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public Exhibit getExhibitById(Long id) {
        return exhibitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exhibit not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Exhibit> searchByName(String name) {
        return exhibitRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public List<Exhibit> getByCategory(String category) {
        return exhibitRepository.findByCategory(category);
    }
}