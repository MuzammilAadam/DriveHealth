package com.example.drivehealth.dto;

import java.time.LocalDateTime;

public class ScanRunResponse {

    private Long id;
    private Long googleAccountId;
    private String scanType;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private int filesScanned;
    private int newFiles;
    private int updatedFiles;
    private int findingsCreated;
    private String errorMessage;

    public ScanRunResponse() {
    }

    public ScanRunResponse(Long id, Long googleAccountId, String scanType, String status,
                           LocalDateTime startedAt, LocalDateTime completedAt,
                           int filesScanned, int newFiles, int updatedFiles,
                           int findingsCreated, String errorMessage) {
        this.id = id;
        this.googleAccountId = googleAccountId;
        this.scanType = scanType;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.filesScanned = filesScanned;
        this.newFiles = newFiles;
        this.updatedFiles = updatedFiles;
        this.findingsCreated = findingsCreated;
        this.errorMessage = errorMessage;
    }

    public Long getId() {
        return id;
    }

    public Long getGoogleAccountId() {
        return googleAccountId;
    }

    public String getScanType() {
        return scanType;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public int getFilesScanned() {
        return filesScanned;
    }

    public int getNewFiles() {
        return newFiles;
    }

    public int getUpdatedFiles() {
        return updatedFiles;
    }

    public int getFindingsCreated() {
        return findingsCreated;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
