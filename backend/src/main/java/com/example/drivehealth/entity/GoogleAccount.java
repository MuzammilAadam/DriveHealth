package com.example.drivehealth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Represents a connected Google Account associated with a User.
 * Stores OAuth tokens and basic Google profile information.
 * 
 * SECURITY NOTE ON TOKENS:
 * In this stage, tokens are stored as database strings for straightforward development.
 * In a production environment:
 * 1. Sensitive tokens (accessToken, refreshToken) must NEVER be returned in REST API responses.
 * 2. They should be encrypted at rest using JPA AttributeConverter with AES-256-GCM,
 *    or managed through a secrets management service such as AWS KMS, HashiCorp Vault,
 *    or Google Cloud Secret Manager.
 */
@Entity
@Table(name = "google_accounts")
public class GoogleAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many Google accounts can belong to one application User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Google's unique subject identifier ('sub' from ID token)
    @Column(name = "google_user_id", nullable = false, unique = true)
    private String googleUserId;

    @Column(nullable = false)
    private String email;

    private String name;

    @Column(name = "picture_url", length = 512)
    private String pictureUrl;

    // OAuth 2.0 Access Token: Used to authorize requests to Google Drive API
    // Stored as TEXT because OAuth tokens can be lengthy
    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    // OAuth 2.0 Refresh Token: Used to obtain a new access token when it expires
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    // Timestamp when the current access token expires
    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    // Granted scopes (e.g. drive.metadata.readonly, email, profile)
    @Column(length = 1024)
    private String scope;

    @Column(name = "connected_at", nullable = false, updatable = false)
    private LocalDateTime connectedAt;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    public GoogleAccount() {
    }

    @PrePersist
    protected void onCreate() {
        this.connectedAt = LocalDateTime.now();
    }

    // Check if the current access token is expired or about to expire in 5 minutes
    public boolean isAccessTokenExpired() {
        if (tokenExpiresAt == null) {
            return true;
        }
        return LocalDateTime.now().plusMinutes(5).isAfter(tokenExpiresAt);
    }

    // Getters and Setters (Plain Java)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public LocalDateTime getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
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
