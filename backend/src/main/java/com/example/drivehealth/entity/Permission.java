package com.example.drivehealth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * Stage 9: Stores permission metadata for a Google Drive file.
 *
 * We record who has access to a file and at what level, so we can
 * identify risky sharing patterns (external users, public links, etc.).
 *
 * We NEVER modify permissions here. The user always decides what action to take.
 */
@Entity
@Table(
    name = "permissions",
    uniqueConstraints = {
        // A Drive file has at most one permission record per permission ID
        @UniqueConstraint(name = "uk_permissions_file_perm", columnNames = {"drive_file_id", "permission_id"})
    },
    indexes = {
        @Index(name = "idx_permissions_account", columnList = "google_account_id"),
        @Index(name = "idx_permissions_file", columnList = "drive_file_id"),
        @Index(name = "idx_permissions_type", columnList = "type"),
        @Index(name = "idx_permissions_role", columnList = "role")
    }
)
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The Google Account this permission belongs to (for query scoping)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "google_account_id", nullable = false)
    private GoogleAccount googleAccount;

    // The Drive file this permission is attached to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drive_file_id", nullable = false)
    private DriveFile driveFile;

    // Google's ID for this permission entry (e.g. "anyoneWithLink", "03aj2vh...")
    @Column(name = "permission_id", nullable = false)
    private String permissionId;

    // Permission type: user | group | domain | anyone
    @Column(name = "type", nullable = false, length = 32)
    private String type;

    // Access level: reader | commenter | writer | owner
    @Column(name = "role", nullable = false, length = 32)
    private String role;

    // Email address of the grantee (null for domain/anyone types)
    @Column(name = "email_address")
    private String emailAddress;

    // Display name of the grantee
    @Column(name = "display_name")
    private String displayName;

    // Domain for domain-type permissions (e.g. "example.com")
    @Column(name = "domain")
    private String domain;

    // When this permission expires (null means no expiry — a risk signal)
    @Column(name = "expiration_time")
    private LocalDateTime expirationTime;

    // For "anyone" type: true means the file is discoverable via search
    @Column(name = "allow_file_discovery")
    private Boolean allowFileDiscovery;

    // When this permission record was indexed locally
    @Column(name = "indexed_at", nullable = false)
    private LocalDateTime indexedAt;

    public Permission() {
    }

    @PrePersist
    protected void onIndex() {
        if (this.indexedAt == null) {
            this.indexedAt = LocalDateTime.now();
        }
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public GoogleAccount getGoogleAccount() { return googleAccount; }
    public void setGoogleAccount(GoogleAccount googleAccount) { this.googleAccount = googleAccount; }

    public DriveFile getDriveFile() { return driveFile; }
    public void setDriveFile(DriveFile driveFile) { this.driveFile = driveFile; }

    public String getPermissionId() { return permissionId; }
    public void setPermissionId(String permissionId) { this.permissionId = permissionId; }

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

    public LocalDateTime getExpirationTime() { return expirationTime; }
    public void setExpirationTime(LocalDateTime expirationTime) { this.expirationTime = expirationTime; }

    public Boolean getAllowFileDiscovery() { return allowFileDiscovery; }
    public void setAllowFileDiscovery(Boolean allowFileDiscovery) { this.allowFileDiscovery = allowFileDiscovery; }

    public LocalDateTime getIndexedAt() { return indexedAt; }
    public void setIndexedAt(LocalDateTime indexedAt) { this.indexedAt = indexedAt; }
}
