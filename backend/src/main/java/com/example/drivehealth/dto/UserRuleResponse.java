package com.example.drivehealth.dto;

import com.example.drivehealth.entity.RuleType;

import java.time.LocalDateTime;

public class UserRuleResponse {

    private Long id;
    private Long googleAccountId;
    private RuleType ruleType;
    private String ruleValue;
    private String description;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserRuleResponse() {
    }

    public UserRuleResponse(Long id, Long googleAccountId, RuleType ruleType, String ruleValue,
                            String description, boolean enabled, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.googleAccountId = googleAccountId;
        this.ruleType = ruleType;
        this.ruleValue = ruleValue;
        this.description = description;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getGoogleAccountId() {
        return googleAccountId;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public String getRuleValue() {
        return ruleValue;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
