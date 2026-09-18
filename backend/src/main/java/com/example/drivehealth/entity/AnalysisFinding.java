package com.example.drivehealth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Stage 6: Universal entity representing a hygiene analysis finding.
 * 
 * Provides a unified data model for all current and future analysis rules:
 * - DUPLICATE
 * - OLD_FILE
 * - LARGE_FILE
 * - EXTERNAL_SHARE
 * - PUBLIC_FILE
 * - POSSIBLE_DUPLICATE
 * - EMPTY_FOLDER
 */
@Entity
@Table(
    name = "analysis_findings",
    indexes = {
        @Index(name = "idx_findings_account", columnList = "google_account_id"),
        @Index(name = "idx_findings_type", columnList = "finding_type"),
        @Index(name = "idx_findings_severity", columnList = "severity"),
        @Index(name = "idx_findings_status", columnList = "status"),
        @Index(name = "idx_findings_file", columnList = "drive_file_id")
    }
)
public class AnalysisFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The Google account this finding belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "google_account_id", nullable = false)
    private GoogleAccount googleAccount;

    // The Drive file this finding relates to (nullable for account-wide findings)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drive_file_id")
    private DriveFile driveFile;

    // Type of hygiene finding
    @Enumerated(EnumType.STRING)
    @Column(name = "finding_type", nullable = false, length = 64)
    private FindingType findingType;

    // Severity level: LOW, MEDIUM, HIGH, CRITICAL
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Severity severity = Severity.MEDIUM;

    // Confidence score (0 to 100)
    @Column(nullable = false)
    private int confidence = 100;

    // Human-readable explanation of why this file was flagged
    // e.g. "File has not been modified for more than 2 years"
    @Column(nullable = false, length = 1000)
    private String reason;

    // Status: OPEN, IGNORED, RESOLVED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private FindingStatus status = FindingStatus.OPEN;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Timestamp when the user marked the finding as RESOLVED or IGNORED
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public AnalysisFinding() {
    }

    public AnalysisFinding(GoogleAccount googleAccount, DriveFile driveFile, FindingType findingType,
                           Severity severity, int confidence, String reason) {
        this.googleAccount = googleAccount;
        this.driveFile = driveFile;
        this.findingType = findingType;
        this.severity = severity;
        this.confidence = confidence;
        this.reason = reason;
        this.status = FindingStatus.OPEN;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = FindingStatus.OPEN;
        }
    }

    // Convenience helper getters
    public Long getGoogleAccountId() {
        return googleAccount != null ? googleAccount.getId() : null;
    }

    public Long getFileId() {
        return driveFile != null ? driveFile.getId() : null;
    }

    // Standard Getters and Setters (Plain Java, no Lombok)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GoogleAccount getGoogleAccount() {
        return googleAccount;
    }

    public void setGoogleAccount(GoogleAccount googleAccount) {
        this.googleAccount = googleAccount;
    }

    public DriveFile getDriveFile() {
        return driveFile;
    }

    public void setDriveFile(DriveFile driveFile) {
        this.driveFile = driveFile;
    }

    public FindingType getFindingType() {
        return findingType;
    }

    public void setFindingType(FindingType findingType) {
        this.findingType = findingType;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public FindingStatus getStatus() {
        return status;
    }

    public void setStatus(FindingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
