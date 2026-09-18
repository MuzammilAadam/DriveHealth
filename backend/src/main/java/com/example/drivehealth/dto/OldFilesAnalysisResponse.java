package com.example.drivehealth.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Top-level response for GET /api/analysis/old-files.
 */
public class OldFilesAnalysisResponse {

    private List<OldFileItemResponse> files = new ArrayList<>();
    private int totalOldFiles;
    private long totalOldStorageBytes;
    private int yearsThreshold;
    private LocalDateTime cutoffDate;

    public OldFilesAnalysisResponse() {
    }

    public OldFilesAnalysisResponse(List<OldFileItemResponse> files, int totalOldFiles,
                                    long totalOldStorageBytes, int yearsThreshold,
                                    LocalDateTime cutoffDate) {
        this.files = files;
        this.totalOldFiles = totalOldFiles;
        this.totalOldStorageBytes = totalOldStorageBytes;
        this.yearsThreshold = yearsThreshold;
        this.cutoffDate = cutoffDate;
    }

    // Standard Getters and Setters

    public List<OldFileItemResponse> getFiles() {
        return files;
    }

    public void setFiles(List<OldFileItemResponse> files) {
        this.files = files;
    }

    public int getTotalOldFiles() {
        return totalOldFiles;
    }

    public void setTotalOldFiles(int totalOldFiles) {
        this.totalOldFiles = totalOldFiles;
    }

    public long getTotalOldStorageBytes() {
        return totalOldStorageBytes;
    }

    public void setTotalOldStorageBytes(long totalOldStorageBytes) {
        this.totalOldStorageBytes = totalOldStorageBytes;
    }

    public int getYearsThreshold() {
        return yearsThreshold;
    }

    public void setYearsThreshold(int yearsThreshold) {
        this.yearsThreshold = yearsThreshold;
    }

    public LocalDateTime getCutoffDate() {
        return cutoffDate;
    }

    public void setCutoffDate(LocalDateTime cutoffDate) {
        this.cutoffDate = cutoffDate;
    }
}
