package com.example.drivehealth.dto;

import java.time.LocalDateTime;

/**
 * Representation of a file within a duplicate group.
 */
public class DuplicateFileItemResponse {

    private Long id;
    private String googleFileId;
    private String name;
    private Long size;
    private String mimeType;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;
    private String webUrl;
    private String parentId;

    public DuplicateFileItemResponse() {
    }

    public DuplicateFileItemResponse(Long id, String googleFileId, String name, Long size,
                                     String mimeType, LocalDateTime createdTime,
                                     LocalDateTime modifiedTime, String webUrl, String parentId) {
        this.id = id;
        this.googleFileId = googleFileId;
        this.name = name;
        this.size = size;
        this.mimeType = mimeType;
        this.createdTime = createdTime;
        this.modifiedTime = modifiedTime;
        this.webUrl = webUrl;
        this.parentId = parentId;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
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
}
