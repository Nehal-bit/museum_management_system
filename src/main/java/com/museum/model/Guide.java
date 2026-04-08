package com.museum.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guides")
@PrimaryKeyJoinColumn(name = "user_id")
public class Guide extends User {

    private String specialization;

    @Column(nullable = false)
    private boolean available = true;

    @JsonIgnore
    @OneToMany(mappedBy = "guide", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GuideAssignment> assignments = new ArrayList<>();

    @PrePersist
    protected void prePersist() {
        this.setRole(UserRole.GUIDE);
    }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public List<GuideAssignment> getAssignments() { return assignments; }
    public void setAssignments(List<GuideAssignment> assignments) { this.assignments = assignments; }
}
