package com.example.drivehealth.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Stage 9: Represents a file that has risky sharing — external users or public access.
 */
public class ExternalShareItemResponse {

    private Long fileId;
    private String googleFileId;
    private String fileName;
    private String mimeType;
    private String webUrl;
    private List<PermissionResponse> riskyPermissions;
    private String reason;

    public ExternalShareItemResponse(Long fileId, String googleFileId, String fileName,
                                      String mimeType, String webUrl,
                                      List<PermissionResponse> riskyPermissions, String reason) {
        this.fileId = fileId;
        this.googleFileId = googleFileId;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.webUrl = webUrl;
        this.riskyPermissions = riskyPermissions;
        this.reason = reason;
    }

    public Long getFileId() { return fileId; }
    public String getGoogleFileId() { return googleFileId; }
    public String getFileName() { return fileName; }
    public String getMimeType() { return mimeType; }
    public String getWebUrl() { return webUrl; }
    public List<PermissionResponse> getRiskyPermissions() { return riskyPermissions; }
    public String getReason() { return reason; }
}
