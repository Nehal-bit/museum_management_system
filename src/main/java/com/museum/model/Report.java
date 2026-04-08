package com.museum.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @Column(nullable = false)
    private String reportType;

    @Column(nullable = false, updatable = false)
    private LocalDateTime generatedOn;

    @Column(columnDefinition = "TEXT")
    private String reportData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by")
    private Admin generatedBy;

    @PrePersist
    protected void prePersist() { this.generatedOn = LocalDateTime.now(); }

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public LocalDateTime getGeneratedOn() { return generatedOn; }
    public void setGeneratedOn(LocalDateTime generatedOn) { this.generatedOn = generatedOn; }
    public String getReportData() { return reportData; }
    public void setReportData(String reportData) { this.reportData = reportData; }
    public Admin getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(Admin generatedBy) { this.generatedBy = generatedBy; }
}
