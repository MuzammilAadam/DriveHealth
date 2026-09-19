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
 * Stage 15: Entity tracking storage history and usage trends over time.
 */
@Entity
@Table(
    name = "storage_snapshots",
    indexes = {
        @Index(name = "idx_storage_snapshots_account", columnList = "google_account_id"),
        @Index(name = "idx_storage_snapshots_time", columnList = "snapshot_time")
    }
)
public class StorageSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "google_account_id", nullable = false)
    private GoogleAccount googleAccount;

    @Column(name = "snapshot_time", nullable = false)
    private LocalDateTime snapshotTime;

    @Column(name = "total_files", nullable = false)
    private long totalFiles;

    @Column(name = "total_storage_bytes", nullable = false)
    private long totalStorageBytes;

    @Column(name = "trashed_files", nullable = false)
    private long trashedFiles;

    @Column(name = "trashed_storage_bytes", nullable = false)
    private long trashedStorageBytes;

    @Column(name = "active_files", nullable = false)
    private long activeFiles;

    @Column(name = "active_storage_bytes", nullable = false)
    private long activeStorageBytes;

    public StorageSnapshot() {
    }

    public StorageSnapshot(GoogleAccount googleAccount, long totalFiles, long totalStorageBytes,
                           long trashedFiles, long trashedStorageBytes,
                           long activeFiles, long activeStorageBytes) {
        this.googleAccount = googleAccount;
        this.totalFiles = totalFiles;
        this.totalStorageBytes = totalStorageBytes;
        this.trashedFiles = trashedFiles;
        this.trashedStorageBytes = trashedStorageBytes;
        this.activeFiles = activeFiles;
        this.activeStorageBytes = activeStorageBytes;
        this.snapshotTime = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.snapshotTime == null) {
            this.snapshotTime = LocalDateTime.now();
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

    public LocalDateTime getSnapshotTime() {
        return snapshotTime;
    }

    public void setSnapshotTime(LocalDateTime snapshotTime) {
        this.snapshotTime = snapshotTime;
    }

    public long getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(long totalFiles) {
        this.totalFiles = totalFiles;
    }

    public long getTotalStorageBytes() {
        return totalStorageBytes;
    }

    public void setTotalStorageBytes(long totalStorageBytes) {
        this.totalStorageBytes = totalStorageBytes;
    }

    public long getTrashedFiles() {
        return trashedFiles;
    }

    public void setTrashedFiles(long trashedFiles) {
        this.trashedFiles = trashedFiles;
    }

    public long getTrashedStorageBytes() {
        return trashedStorageBytes;
    }

    public void setTrashedStorageBytes(long trashedStorageBytes) {
        this.trashedStorageBytes = trashedStorageBytes;
    }

    public long getActiveFiles() {
        return activeFiles;
    }

    public void setActiveFiles(long activeFiles) {
        this.activeFiles = activeFiles;
    }

    public long getActiveStorageBytes() {
        return activeStorageBytes;
    }

    public void setActiveStorageBytes(long activeStorageBytes) {
        this.activeStorageBytes = activeStorageBytes;
    }
}
