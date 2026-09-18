package com.example.drivehealth.dto;

import java.time.LocalDateTime;

/**
 * DTO representing an old file identified during hygiene analysis.
 */
public class OldFileItemResponse {

    private Long id;
    private String googleFileId;
    private String name;
    private String mimeType;
    private Long size;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;
    private long daysSinceModified;
    private String webUrl;
    private String parentId;
    private String reason;

    public OldFileItemResponse() {
    }

    public OldFileItemResponse(Long id, String googleFileId, String name, String mimeType,
                               Long size, LocalDateTime createdTime, LocalDateTime modifiedTime,
                               long daysSinceModified, String webUrl, String parentId, String reason) {
        this.id = id;
        this.googleFileId = googleFileId;
        this.name = name;
        this.mimeType = mimeType;
        this.size = size;
        this.createdTime = createdTime;
        this.modifiedTime = modifiedTime;
        this.daysSinceModified = daysSinceModified;
        this.webUrl = webUrl;
        this.parentId = parentId;
        this.reason = reason;
    }

    // Standard Getters and Setters

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

    public long getDaysSinceModified() {
        return daysSinceModified;
    }

    public void setDaysSinceModified(long daysSinceModified) {
        this.daysSinceModified = daysSinceModified;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
