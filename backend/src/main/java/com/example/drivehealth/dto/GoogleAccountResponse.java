package com.example.drivehealth.dto;

import java.time.LocalDateTime;

/**
 * Safe public representation of a connected Google Account.
 * SECURITY: NEVER expose accessToken or refreshToken in this DTO!
 */
public class GoogleAccountResponse {

    private Long id;
    private String googleUserId;
    private String email;
    private String name;
    private String pictureUrl;
    private LocalDateTime connectedAt;
    private LocalDateTime lastSyncedAt;

    public GoogleAccountResponse() {
    }

    public GoogleAccountResponse(Long id, String googleUserId, String email, String name, 
                                 String pictureUrl, LocalDateTime connectedAt, LocalDateTime lastSyncedAt) {
        this.id = id;
        this.googleUserId = googleUserId;
        this.email = email;
        this.name = name;
        this.pictureUrl = pictureUrl;
        this.connectedAt = connectedAt;
        this.lastSyncedAt = lastSyncedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGoogleUserId() {
        return googleUserId;
    }

    public void setGoogleUserId(String googleUserId) {
        this.googleUserId = googleUserId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(LocalDateTime connectedAt) {
        this.connectedAt = connectedAt;
    }

    public LocalDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(LocalDateTime lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }
}
