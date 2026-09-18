package com.example.drivehealth.dto;

import com.example.drivehealth.entity.FindingStatus;
import com.example.drivehealth.entity.FindingType;
import com.example.drivehealth.entity.Severity;

import java.time.LocalDateTime;

/**
 * Stage 6: DTO representing an individual hygiene analysis finding.
 * 
 * Example JSON representation:
 * {
 *   "id": 1,
 *   "googleAccountId": 1,
 *   "fileId": 42,
 *   "googleFileId": "1a2b3c...",
 *   "fileName": "Project_2022.zip",
 *   "findingType": "OLD_FILE",
 *   "severity": "MEDIUM",
 *   "confidence": 100,
 *   "reason": "File has not been modified for more than 2 years",
 *   "status": "OPEN",
 *   "createdAt": "2026-09-12T18:30:00"
 * }
 */
public class AnalysisFindingResponse {

    private Long id;
    private Long googleAccountId;
    private Long fileId;
    private String googleFileId;
    private String fileName;
    private FindingType findingType;
    private Severity severity;
    private int confidence;
    private String reason;
    private FindingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public AnalysisFindingResponse() {
    }

    public AnalysisFindingResponse(Long id, Long googleAccountId, Long fileId, String googleFileId,
                                   String fileName, FindingType findingType, Severity severity,
                                   int confidence, String reason, FindingStatus status,
                                   LocalDateTime createdAt, LocalDateTime resolvedAt) {
        this.id = id;
        this.googleAccountId = googleAccountId;
        this.fileId = fileId;
        this.googleFileId = googleFileId;
        this.fileName = fileName;
        this.findingType = findingType;
        this.severity = severity;
        this.confidence = confidence;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }

    // Standard Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGoogleAccountId() {
        return googleAccountId;
    }

    public void setGoogleAccountId(Long googleAccountId) {
        this.googleAccountId = googleAccountId;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getGoogleFileId() {
        return googleFileId;
    }

    public void setGoogleFileId(String googleFileId) {
        this.googleFileId = googleFileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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
