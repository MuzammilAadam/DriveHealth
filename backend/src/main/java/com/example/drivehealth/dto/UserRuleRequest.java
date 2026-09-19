package com.example.drivehealth.dto;

import com.example.drivehealth.entity.RuleType;

public class UserRuleRequest {

    private RuleType ruleType;
    private String ruleValue;
    private String description;
    private Boolean enabled;

    public UserRuleRequest() {
    }

    public UserRuleRequest(RuleType ruleType, String ruleValue, String description) {
        this.ruleType = ruleType;
        this.ruleValue = ruleValue;
        this.description = description;
        this.enabled = true;
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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
