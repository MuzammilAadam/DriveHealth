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
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * Represents metadata of a Google Drive file or folder stored locally in MySQL.
 * 
 * IMPORTANT DATA PRINCIPLE:
 * We do NOT store the actual file content (PDFs, videos, docs, images).
 * Google Drive remains the source of truth for the files themselves.
 * We only index file metadata to perform hygiene analysis (duplicate detection,
 * old file detection, large file detection, and permission checks).
 */
@Entity
@Table(
    name = "drive_files",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_drive_files_account_file", columnNames = {"google_account_id", "google_file_id"})
    },
    indexes = {
        @Index(name = "idx_drive_files_account", columnList = "google_account_id"),
        @Index(name = "idx_drive_files_checksum", columnList = "md5_checksum"),
        @Index(name = "idx_drive_files_size", columnList = "size"),
        @Index(name = "idx_drive_files_modified", columnList = "modified_time")
    }
)
public class DriveFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many files belong to one GoogleAccount
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "google_account_id", nullable = false)
    private GoogleAccount googleAccount;

    // Unique file identifier assigned by Google Drive
    @Column(name = "google_file_id", nullable = false)
    private String googleFileId;

    // File or folder name
    @Column(name = "name", length = 1000)
    private String name;

    // MIME type (e.g. application/pdf, application/vnd.google-apps.folder)
    @Column(name = "mime_type")
    private String mimeType;

    // File size in bytes (folders and native Google Docs usually have size = 0 or null)
    @Column(name = "size")
    private Long size;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "modified_time")
    private LocalDateTime modifiedTime;

    // Web link to view or open the file in Google Drive
    @Column(name = "web_url", length = 1024)
    private String webUrl;

    // Google file ID of parent folder
    @Column(name = "parent_id")
    private String parentId;

    // Email of primary owner
    @Column(name = "owner_email")
    private String ownerEmail;

    // MD5 checksum provided by Google Drive (key signal for exact duplicate detection)
    @Column(name = "md5_checksum", length = 64)
    private String md5Checksum;

    // Whether file is moved to Google Drive trash
    @Column(name = "trashed")
    private Boolean trashed = false;

    // Timestamp when this metadata record was locally indexed/updated
    @Column(name = "indexed_at", nullable = false)
    private LocalDateTime indexedAt;

    public DriveFile() {
    }

    @PrePersist
    protected void onIndex() {
        if (this.indexedAt == null) {
            this.indexedAt = LocalDateTime.now();
        }
        if (this.trashed == null) {
            this.trashed = false;
        }
        if (this.size == null) {
            this.size = 0L;
        }
    }

    // Helper to get account ID directly without loading entire GoogleAccount proxy
    public Long getGoogleAccountId() {
        return googleAccount != null ? googleAccount.getId() : null;
    }

    // Standard Getters and Setters (Plain Java, no Lombok magic)

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

    public String getGoogleFileId() {
        return googleFileId;
    }

    public void setGoogleFileId(String googleFileId) {
        this.googleFileId = googleFileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(LocalDateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getMd5Checksum() {
        return md5Checksum;
    }

    public void setMd5Checksum(String md5Checksum) {
        this.md5Checksum = md5Checksum;
    }

    public Boolean getTrashed() {
        return trashed;
    }

    public void setTrashed(Boolean trashed) {
        this.trashed = trashed;
    }

    public LocalDateTime getIndexedAt() {
        return indexedAt;
    }

    public void setIndexedAt(LocalDateTime indexedAt) {
        this.indexedAt = indexedAt;
    }
}
