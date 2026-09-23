package com.example.drivehealth.dto;

import java.time.LocalDateTime;

public class StorageQuotaResponse {

    private Long accountId;
    private String email;
    private Long limitBytes;
    private Long usageBytes;
    private Long usageInDriveBytes;
    private Long usageInDriveTrashBytes;
    private Double usedPercent;
    private LocalDateTime lastUpdated;

    public StorageQuotaResponse() {
    }

    public StorageQuotaResponse(Long accountId, String email, Long limitBytes, Long usageBytes,
                                Long usageInDriveBytes, Long usageInDriveTrashBytes, LocalDateTime lastUpdated) {
        this.accountId = accountId;
        this.email = email;
        this.limitBytes = limitBytes;
        this.usageBytes = usageBytes;
        this.usageInDriveBytes = usageInDriveBytes;
        this.usageInDriveTrashBytes = usageInDriveTrashBytes;
        this.lastUpdated = lastUpdated;

        if (limitBytes != null && limitBytes > 0 && usageBytes != null) {
            this.usedPercent = Math.min(100.0, Math.round(((double) usageBytes / limitBytes) * 10000.0) / 100.0);
        } else {
            this.usedPercent = 0.0;
        }
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getLimitBytes() {
        return limitBytes;
    }

    public void setLimitBytes(Long limitBytes) {
        this.limitBytes = limitBytes;
    }

    public Long getUsageBytes() {
        return usageBytes;
    }

    public void setUsageBytes(Long usageBytes) {
        this.usageBytes = usageBytes;
    }

    public Long getUsageInDriveBytes() {
        return usageInDriveBytes;
    }

    public void setUsageInDriveBytes(Long usageInDriveBytes) {
        this.usageInDriveBytes = usageInDriveBytes;
    }

    public Long getUsageInDriveTrashBytes() {
        return usageInDriveTrashBytes;
    }

    public void setUsageInDriveTrashBytes(Long usageInDriveTrashBytes) {
        this.usageInDriveTrashBytes = usageInDriveTrashBytes;
    }

    public Double getUsedPercent() {
        return usedPercent;
    }

    public void setUsedPercent(Double usedPercent) {
        this.usedPercent = usedPercent;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
