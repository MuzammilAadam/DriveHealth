package com.example.drivehealth.dto;

/**
 * Stage 10: Aggregated dashboard summary for a Google Account.
 */
public class DashboardSummaryResponse {

    private Long googleAccountId;
    private String accountEmail;

    // File counts
    private long totalFiles;
    private long totalStorageBytes;
    private double totalStorageMb;
    private long trashedFiles;

    // Analysis finding counts
    private long totalFindings;
    private long openFindings;
    private long ignoredFindings;
    private long resolvedFindings;
    private long highSeverityOpenFindings;

    // Category counts
    private long duplicateGroups;
    private long duplicateFiles;
    private long oldFiles;
    private long largeFiles;
    private long externalShares;
    private long publicFiles;

    // Google Drive Live Storage Quota
    private Long storageQuotaLimit;
    private Long storageQuotaUsage;
    private Long storageQuotaUsageInDrive;
    private Long storageQuotaUsageInDriveTrash;

    public DashboardSummaryResponse(Long googleAccountId, String accountEmail,
                                     long totalFiles, long totalStorageBytes, long trashedFiles,
                                     long totalFindings, long openFindings, long ignoredFindings,
                                     long resolvedFindings, long highSeverityOpenFindings,
                                     long duplicateGroups, long duplicateFiles,
                                     long oldFiles, long largeFiles,
                                     long externalShares, long publicFiles) {
        this(googleAccountId, accountEmail, totalFiles, totalStorageBytes, trashedFiles,
             totalFindings, openFindings, ignoredFindings, resolvedFindings, highSeverityOpenFindings,
             duplicateGroups, duplicateFiles, oldFiles, largeFiles, externalShares, publicFiles,
             null, null, null, null);
    }

    public DashboardSummaryResponse(Long googleAccountId, String accountEmail,
                                     long totalFiles, long totalStorageBytes, long trashedFiles,
                                     long totalFindings, long openFindings, long ignoredFindings,
                                     long resolvedFindings, long highSeverityOpenFindings,
                                     long duplicateGroups, long duplicateFiles,
                                     long oldFiles, long largeFiles,
                                     long externalShares, long publicFiles,
                                     Long storageQuotaLimit, Long storageQuotaUsage,
                                     Long storageQuotaUsageInDrive, Long storageQuotaUsageInDriveTrash) {
        this.googleAccountId = googleAccountId;
        this.accountEmail = accountEmail;
        this.totalFiles = totalFiles;
        this.totalStorageBytes = totalStorageBytes;
        this.totalStorageMb = Math.round((totalStorageBytes / (1024.0 * 1024.0)) * 100.0) / 100.0;
        this.trashedFiles = trashedFiles;
        this.totalFindings = totalFindings;
        this.openFindings = openFindings;
        this.ignoredFindings = ignoredFindings;
        this.resolvedFindings = resolvedFindings;
        this.highSeverityOpenFindings = highSeverityOpenFindings;
        this.duplicateGroups = duplicateGroups;
        this.duplicateFiles = duplicateFiles;
        this.oldFiles = oldFiles;
        this.largeFiles = largeFiles;
        this.externalShares = externalShares;
        this.publicFiles = publicFiles;
        this.storageQuotaLimit = storageQuotaLimit;
        this.storageQuotaUsage = storageQuotaUsage;
        this.storageQuotaUsageInDrive = storageQuotaUsageInDrive;
        this.storageQuotaUsageInDriveTrash = storageQuotaUsageInDriveTrash;
    }

    public Long getGoogleAccountId() { return googleAccountId; }
    public String getAccountEmail() { return accountEmail; }
    public long getTotalFiles() { return totalFiles; }
    public long getTotalStorageBytes() { return totalStorageBytes; }
    public double getTotalStorageMb() { return totalStorageMb; }
    public long getTrashedFiles() { return trashedFiles; }
    public long getTotalFindings() { return totalFindings; }
    public long getOpenFindings() { return openFindings; }
    public long getIgnoredFindings() { return ignoredFindings; }
    public long getResolvedFindings() { return resolvedFindings; }
    public long getHighSeverityOpenFindings() { return highSeverityOpenFindings; }
    public long getDuplicateGroups() { return duplicateGroups; }
    public long getDuplicateFiles() { return duplicateFiles; }
    public long getOldFiles() { return oldFiles; }
    public long getLargeFiles() { return largeFiles; }
    public long getExternalShares() { return externalShares; }
    public long getPublicFiles() { return publicFiles; }

    public Long getStorageQuotaLimit() { return storageQuotaLimit; }
    public Long getStorageQuotaUsage() { return storageQuotaUsage; }
    public Long getStorageQuotaUsageInDrive() { return storageQuotaUsageInDrive; }
    public Long getStorageQuotaUsageInDriveTrash() { return storageQuotaUsageInDriveTrash; }
}
