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
 * Stage 5: Join entity linking a DuplicateGroup to an individual DriveFile.
 * 
 * Hierarchy:
 * DuplicateGroup
 *   ↓
 * DuplicateGroupFile
 *   ↓
 * DriveFile
 */
@Entity
@Table(
    name = "duplicate_group_files",
    indexes = {
        @Index(name = "idx_dup_group_file_group", columnList = "duplicate_group_id"),
        @Index(name = "idx_dup_group_file_drive_file", columnList = "drive_file_id")
    }
)
public class DuplicateGroupFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The group to which this file belongs
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duplicate_group_id", nullable = false)
    private DuplicateGroup duplicateGroup;

    // The indexed DriveFile metadata record
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drive_file_id", nullable = false)
    private DriveFile driveFile;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public DuplicateGroupFile() {
    }

    public DuplicateGroupFile(DuplicateGroup duplicateGroup, DriveFile driveFile) {
        this.duplicateGroup = duplicateGroup;
        this.driveFile = driveFile;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Standard Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DuplicateGroup getDuplicateGroup() {
        return duplicateGroup;
    }

    public void setDuplicateGroup(DuplicateGroup duplicateGroup) {
        this.duplicateGroup = duplicateGroup;
    }

    public DriveFile getDriveFile() {
        return driveFile;
    }

    public void setDriveFile(DriveFile driveFile) {
        this.driveFile = driveFile;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
