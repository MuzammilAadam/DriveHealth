package com.example.drivehealth.dto;

import java.util.List;

/**
 * Stage 8: Summary response for the large file analysis.
 */
public class LargeFileAnalysisResponse {

    private List<LargeFileItemResponse> files;
    private int totalLargeFiles;
    private long totalLargeStorageBytes;
    private double totalLargeStorageMb;
    private long thresholdBytes;
    private double thresholdMb;

    public LargeFileAnalysisResponse(List<LargeFileItemResponse> files,
                                      int totalLargeFiles,
                                      long totalLargeStorageBytes,
                                      long thresholdBytes) {
        this.files = files;
        this.totalLargeFiles = totalLargeFiles;
        this.totalLargeStorageBytes = totalLargeStorageBytes;
        this.totalLargeStorageMb = Math.round((totalLargeStorageBytes / (1024.0 * 1024.0)) * 100.0) / 100.0;
        this.thresholdBytes = thresholdBytes;
        this.thresholdMb = Math.round((thresholdBytes / (1024.0 * 1024.0)) * 100.0) / 100.0;
    }

    public List<LargeFileItemResponse> getFiles() { return files; }
    public int getTotalLargeFiles() { return totalLargeFiles; }
    public long getTotalLargeStorageBytes() { return totalLargeStorageBytes; }
    public double getTotalLargeStorageMb() { return totalLargeStorageMb; }
    public long getThresholdBytes() { return thresholdBytes; }
    public double getThresholdMb() { return thresholdMb; }
}
