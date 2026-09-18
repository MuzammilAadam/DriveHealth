package com.example.drivehealth.dto;

import java.time.LocalDateTime;

/**
 * Stage 8: Represents a single large file found during analysis.
 */
public class LargeFileItemResponse {

    private Long id;
    private String googleFileId;
    private String name;
    private String mimeType;
    private long sizeBytes;
    private double sizeMb;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;
    private String webUrl;
    private String parentId;
    private String reason;

    public LargeFileItemResponse(Long id, String googleFileId, String name, String mimeType,
                                  long sizeBytes, LocalDateTime createdTime, LocalDateTime modifiedTime,
                                  String webUrl, String parentId, String reason) {
        this.id = id;
        this.googleFileId = googleFileId;
        this.name = name;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.sizeMb = Math.round((sizeBytes / (1024.0 * 1024.0)) * 100.0) / 100.0;
        this.createdTime = createdTime;
        this.modifiedTime = modifiedTime;
        this.webUrl = webUrl;
        this.parentId = parentId;
        this.reason = reason;
    }

    public Long getId() { return id; }
    public String getGoogleFileId() { return googleFileId; }
    public String getName() { return name; }
    public String getMimeType() { return mimeType; }
    public long getSizeBytes() { return sizeBytes; }
    public double getSizeMb() { return sizeMb; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public LocalDateTime getModifiedTime() { return modifiedTime; }
    public String getWebUrl() { return webUrl; }
    public String getParentId() { return parentId; }
    public String getReason() { return reason; }
}
