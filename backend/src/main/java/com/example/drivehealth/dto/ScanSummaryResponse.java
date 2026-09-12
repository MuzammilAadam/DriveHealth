package com.example.drivehealth.dto;

import java.time.LocalDateTime;

/**
 * Summary DTO returned after performing a Google Drive metadata scan.
 * Reports total files scanned, new files inserted, and existing files updated.
 */
public class ScanSummaryResponse {

    private int filesScanned;
    private int newFiles;
    private int updatedFiles;
    private LocalDateTime scannedAt;

    public ScanSummaryResponse() {
    }

    public ScanSummaryResponse(int filesScanned, int newFiles, int updatedFiles) {
        this.filesScanned = filesScanned;
        this.newFiles = newFiles;
        this.updatedFiles = updatedFiles;
        this.scannedAt = LocalDateTime.now();
    }

    // Getters and Setters

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

    public LocalDateTime getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(LocalDateTime scannedAt) {
        this.scannedAt = scannedAt;
    }
}
