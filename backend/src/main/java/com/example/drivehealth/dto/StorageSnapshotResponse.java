package com.example.drivehealth.dto;

import java.time.LocalDateTime;

public class StorageSnapshotResponse {

    private Long id;
    private Long googleAccountId;
    private LocalDateTime snapshotTime;
    private long totalFiles;
    private long totalStorageBytes;
    private double totalStorageMb;
    private long trashedFiles;
    private long trashedStorageBytes;
    private long activeFiles;
    private long activeStorageBytes;

    public StorageSnapshotResponse() {
    }

    public StorageSnapshotResponse(Long id, Long googleAccountId, LocalDateTime snapshotTime,
                                   long totalFiles, long totalStorageBytes,
                                   long trashedFiles, long trashedStorageBytes,
                                   long activeFiles, long activeStorageBytes) {
        this.id = id;
        this.googleAccountId = googleAccountId;
        this.snapshotTime = snapshotTime;
        this.totalFiles = totalFiles;
        this.totalStorageBytes = totalStorageBytes;
        this.totalStorageMb = Math.round((totalStorageBytes / (1024.0 * 1024.0)) * 100.0) / 100.0;
        this.trashedFiles = trashedFiles;
        this.trashedStorageBytes = trashedStorageBytes;
        this.activeFiles = activeFiles;
        this.activeStorageBytes = activeStorageBytes;
    }

    public Long getId() {
        return id;
    }

    public Long getGoogleAccountId() {
        return googleAccountId;
    }

    public LocalDateTime getSnapshotTime() {
        return snapshotTime;
    }

    public long getTotalFiles() {
        return totalFiles;
    }

    public long getTotalStorageBytes() {
        return totalStorageBytes;
    }

    public double getTotalStorageMb() {
        return totalStorageMb;
    }

    public long getTrashedFiles() {
        return trashedFiles;
    }

    public long getTrashedStorageBytes() {
        return trashedStorageBytes;
    }

    public long getActiveFiles() {
        return activeFiles;
    }

    public long getActiveStorageBytes() {
        return activeStorageBytes;
    }
}
