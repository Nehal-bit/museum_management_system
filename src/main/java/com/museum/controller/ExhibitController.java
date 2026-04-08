package com.museum.controller;

import com.museum.model.Exhibit;
import com.museum.service.ExhibitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exhibits")
public class ExhibitController {

    private final ExhibitService exhibitService;

    public ExhibitController(ExhibitService exhibitService) {
        this.exhibitService = exhibitService;
    }

    // GET /api/exhibits  AND  /api/exhibits/  (trailing slash safe)
    @GetMapping({"", "/"})
    public ResponseEntity<List<Exhibit>> getAllExhibits() {
        return ResponseEntity.ok(exhibitService.getAllExhibits());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Exhibit>> getActiveExhibits() {
        return ResponseEntity.ok(exhibitService.getActiveExhibits());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getExhibitById(@PathVariable Long id) {
        try { return ResponseEntity.ok(exhibitService.getExhibitById(id)); }
        catch (RuntimeException e) { return ResponseEntity.notFound().build(); }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Exhibit>> searchExhibits(@RequestParam String name) {
        return ResponseEntity.ok(exhibitService.searchByName(name));
    }

    @GetMapping("/category")
    public ResponseEntity<List<Exhibit>> getByCategory(@RequestParam String category) {
        return ResponseEntity.ok(exhibitService.getByCategory(category));
    }

    // POST /api/exhibits  — used by admin panel
    @PostMapping({"", "/"})
    public ResponseEntity<?> addExhibit(@RequestBody Exhibit exhibit) {
        try {
            Exhibit saved = exhibitService.addExhibit(exhibit);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateExhibit(@PathVariable Long id, @RequestBody Exhibit exhibit) {
        try { return ResponseEntity.ok(exhibitService.updateExhibit(id, exhibit)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExhibit(@PathVariable Long id) {
        try { exhibitService.deleteExhibit(id); return ResponseEntity.ok(Map.of("message", "Exhibit archived.")); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }
}
