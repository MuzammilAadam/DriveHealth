package com.example.drivehealth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Stage 14: Entity to track scan executions and history.
 */
@Entity
@Table(
    name = "scan_runs",
    indexes = {
        @Index(name = "idx_scan_runs_account", columnList = "google_account_id"),
        @Index(name = "idx_scan_runs_started", columnList = "started_at")
    }
)
public class ScanRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "google_account_id", nullable = false)
    private GoogleAccount googleAccount;

    @Column(name = "scan_type", nullable = false, length = 50)
    private String scanType; // FULL, INCREMENTAL, SCHEDULED

    @Column(nullable = false, length = 50)
    private String status; // IN_PROGRESS, COMPLETED, FAILED

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "files_scanned", nullable = false)
    private int filesScanned = 0;

    @Column(name = "new_files", nullable = false)
    private int newFiles = 0;

    @Column(name = "updated_files", nullable = false)
    private int updatedFiles = 0;

    @Column(name = "findings_created", nullable = false)
    private int findingsCreated = 0;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    public ScanRun() {
    }

    public ScanRun(GoogleAccount googleAccount, String scanType) {
        this.googleAccount = googleAccount;
        this.scanType = scanType;
        this.status = "IN_PROGRESS";
        this.startedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.startedAt == null) {
            this.startedAt = LocalDateTime.now();
        }
    }

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

    public Long getGoogleAccountId() {
        return googleAccount != null ? googleAccount.getId() : null;
    }

    public String getScanType() {
        return scanType;
    }

    public void setScanType(String scanType) {
        this.scanType = scanType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public int getFilesScanned() {
        return filesScanned;
    }

    public void setFilesScanned(int filesScanned) {
        this.filesScanned = filesScanned;
    }

    public int getNewFiles() {
        return newFiles;
    }

    public void setNewFiles(int newFiles) {
        this.newFiles = newFiles;
    }

    public int getUpdatedFiles() {
        return updatedFiles;
    }

    public void setUpdatedFiles(int updatedFiles) {
        this.updatedFiles = updatedFiles;
    }

    public int getFindingsCreated() {
        return findingsCreated;
    }

    public void setFindingsCreated(int findingsCreated) {
        this.findingsCreated = findingsCreated;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
