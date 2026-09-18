package com.example.drivehealth.dto;

import java.time.LocalDateTime;

/**
 * Stage 9: DTO representing a single permission entry on a Drive file.
 */
public class PermissionResponse {

    private Long id;
    private String permissionId;
    private String type;
    private String role;
    private String emailAddress;
    private String displayName;
    private String domain;
    private LocalDateTime expirationTime;
    private Boolean allowFileDiscovery;

    public PermissionResponse(Long id, String permissionId, String type, String role,
                               String emailAddress, String displayName, String domain,
                               LocalDateTime expirationTime, Boolean allowFileDiscovery) {
        this.id = id;
        this.permissionId = permissionId;
        this.type = type;
        this.role = role;
        this.emailAddress = emailAddress;
        this.displayName = displayName;
        this.domain = domain;
        this.expirationTime = expirationTime;
        this.allowFileDiscovery = allowFileDiscovery;
    }

    public Long getId() { return id; }
    public String getPermissionId() { return permissionId; }
    public String getType() { return type; }
    public String getRole() { return role; }
    public String getEmailAddress() { return emailAddress; }
    public String getDisplayName() { return displayName; }
    public String getDomain() { return domain; }
    public LocalDateTime getExpirationTime() { return expirationTime; }
    public Boolean getAllowFileDiscovery() { return allowFileDiscovery; }
}
