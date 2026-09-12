package com.example.drivehealth.dto;

import java.time.LocalDateTime;

/**
 * DTO representing an indexed Google Drive file stored locally in MySQL.
 */
public class DriveFileResponse {

    private Long id;
    private String googleFileId;
    private Long googleAccountId;
    private String name;
    private String mimeType;
    private Long size;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;
    private String webUrl;
    private String parentId;
    private String ownerEmail;
    private String md5Checksum;
    private Boolean trashed;
    private LocalDateTime indexedAt;

    public DriveFileResponse() {
    }

    public DriveFileResponse(Long id, String googleFileId, Long googleAccountId, String name,
                             String mimeType, Long size, LocalDateTime createdTime,
                             LocalDateTime modifiedTime, String webUrl, String parentId,
                             String ownerEmail, String md5Checksum, Boolean trashed,
                             LocalDateTime indexedAt) {
        this.id = id;
        this.googleFileId = googleFileId;
        this.googleAccountId = googleAccountId;
        this.name = name;
        this.mimeType = mimeType;
        this.size = size;
        this.createdTime = createdTime;
        this.modifiedTime = modifiedTime;
        this.webUrl = webUrl;
        this.parentId = parentId;
        this.ownerEmail = ownerEmail;
        this.md5Checksum = md5Checksum;
        this.trashed = trashed;
        this.indexedAt = indexedAt;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGoogleFileId() {
        return googleFileId;
    }

    public void setGoogleFileId(String googleFileId) {
        this.googleFileId = googleFileId;
    }

    public Long getGoogleAccountId() {
        return googleAccountId;
    }

    public void setGoogleAccountId(Long googleAccountId) {
        this.googleAccountId = googleAccountId;
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
