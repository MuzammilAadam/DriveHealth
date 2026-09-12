package com.example.drivehealth.dto;

import java.time.LocalDateTime;

/**
 * DTO representing metadata for a Google Drive file or folder.
 * NOTE: We do NOT store or download file content, only metadata.
 */
public class DriveFileDto {

    private String id;
    private String name;
    private String mimeType;
    private Long size;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;
    private String parentId;
    private String webUrl;
    private String md5Checksum;
    private Boolean trashed;
    private String ownerEmail;

    public DriveFileDto() {
    }

    public DriveFileDto(String id, String name, String mimeType, Long size,
                        LocalDateTime createdTime, LocalDateTime modifiedTime,
                        String parentId, String webUrl, String md5Checksum,
                        Boolean trashed, String ownerEmail) {
        this.id = id;
        this.name = name;
        this.mimeType = mimeType;
        this.size = size;
        this.createdTime = createdTime;
        this.modifiedTime = modifiedTime;
        this.parentId = parentId;
        this.webUrl = webUrl;
        this.md5Checksum = md5Checksum;
        this.trashed = trashed;
        this.ownerEmail = ownerEmail;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
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

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
}
