package com.example.drivehealth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Stage 13: Entity representing a user-defined hygiene rule.
 * 
 * Examples:
 * - IGNORE_FOLDER: Folder ID to exclude from analysis (e.g. "archive", "backups")
 * - IGNORE_MIME_TYPE: File type to exclude (e.g. "application/pdf")
 * - LARGE_FILE_THRESHOLD: Custom size threshold in bytes (e.g. "1073741824" for 1GB)
 * - OLD_FILE_YEARS: Custom age threshold in years (e.g. "3")
 */
@Entity
@Table(
    name = "user_rules",
    indexes = {
        @Index(name = "idx_user_rules_account", columnList = "google_account_id"),
        @Index(name = "idx_user_rules_type", columnList = "rule_type")
    }
)
public class UserRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "google_account_id", nullable = false)
    private GoogleAccount googleAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 50)
    private RuleType ruleType;

    @Column(name = "rule_value", nullable = false, length = 255)
    private String ruleValue;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserRule() {
    }

    public UserRule(GoogleAccount googleAccount, RuleType ruleType, String ruleValue, String description) {
        this.googleAccount = googleAccount;
        this.ruleType = ruleType;
        this.ruleValue = ruleValue;
        this.description = description;
        this.enabled = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GoogleAccount getGoogleAccount() {
        return googleAccount;
    }

    public void setGoogleAccount(GoogleAccount googleAccount) {
        this.googleAccount = googleAccount;
    }

    public Long getGoogleAccountId() {
        return googleAccount != null ? googleAccount.getId() : null;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    public String getRuleValue() {
        return ruleValue;
    }

    public void setRuleValue(String ruleValue) {
        this.ruleValue = ruleValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
