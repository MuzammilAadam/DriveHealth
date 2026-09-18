package com.example.drivehealth.dto.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Stage 9: Maps a raw Google Drive permission object from the Drive v3 API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleDrivePermission {

    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("role")
    private String role;

    @JsonProperty("emailAddress")
    private String emailAddress;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("domain")
    private String domain;

    @JsonProperty("expirationTime")
    private String expirationTime;

    @JsonProperty("allowFileDiscovery")
    private Boolean allowFileDiscovery;

    public GoogleDrivePermission() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getExpirationTime() { return expirationTime; }
    public void setExpirationTime(String expirationTime) { this.expirationTime = expirationTime; }

    public Boolean getAllowFileDiscovery() { return allowFileDiscovery; }
    public void setAllowFileDiscovery(Boolean allowFileDiscovery) { this.allowFileDiscovery = allowFileDiscovery; }
}
