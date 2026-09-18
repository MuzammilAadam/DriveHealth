package com.example.drivehealth.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage 5: Entity representing a group of duplicate files.
 * 
 * In Stage 5, duplicates are identified when two or more files share
 * the exact same MD5 checksum.
 */
@Entity
@Table(
    name = "duplicate_groups",
    indexes = {
        @Index(name = "idx_dup_group_account", columnList = "google_account_id"),
        @Index(name = "idx_dup_group_checksum", columnList = "md5_checksum")
    }
)
public class DuplicateGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The Google Account to which these duplicate files belong
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "google_account_id", nullable = false)
    private GoogleAccount googleAccount;

    // Confidence percentage (e.g. 100 for exact checksum match)
    @Column(nullable = false)
    private int confidence = 100;

    // Clear explanation of why this group was marked as duplicate
    // Example: "Files have the same checksum"
    @Column(nullable = false)
    private String reason;

    // Shared MD5 checksum for this duplicate group
    @Column(name = "md5_checksum", length = 64)
    private String md5Checksum;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Group relationship: DuplicateGroup -> DuplicateGroupFile -> DriveFile
    @OneToMany(mappedBy = "duplicateGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DuplicateGroupFile> groupFiles = new ArrayList<>();

    public DuplicateGroup() {
    }

    public DuplicateGroup(GoogleAccount googleAccount, int confidence, String reason, String md5Checksum) {
        this.googleAccount = googleAccount;
        this.confidence = confidence;
        this.reason = reason;
        this.md5Checksum = md5Checksum;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Helper methods to maintain bidirectional relationship with DuplicateGroupFile
    public void addGroupFile(DuplicateGroupFile groupFile) {
        groupFiles.add(groupFile);
        groupFile.setDuplicateGroup(this);
    }

    public void removeGroupFile(DuplicateGroupFile groupFile) {
        groupFiles.remove(groupFile);
        groupFile.setDuplicateGroup(null);
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

    public String getMd5Checksum() {
        return md5Checksum;
    }

    public void setMd5Checksum(String md5Checksum) {
        this.md5Checksum = md5Checksum;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<DuplicateGroupFile> getGroupFiles() {
        return groupFiles;
    }

    public void setGroupFiles(List<DuplicateGroupFile> groupFiles) {
        this.groupFiles = groupFiles;
    }
}
