package com.example.drivehealth.service;

import com.example.drivehealth.entity.GoogleAccount;
import com.example.drivehealth.repository.GoogleAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Stage 16: Background scheduler that periodically runs hygiene scans and analysis
 * for all connected Google Accounts.
 */
@Service
public class ScheduledScanService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledScanService.class);

    @Value("${scheduler.scan.enabled:true}")
    private boolean schedulerEnabled;

    private final GoogleAccountRepository googleAccountRepository;
    private final GoogleDriveService googleDriveService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final OldFileAnalysisService oldFileAnalysisService;
    private final LargeFileAnalysisService largeFileAnalysisService;
    private final PermissionAnalysisService permissionAnalysisService;

    public ScheduledScanService(GoogleAccountRepository googleAccountRepository,
                                GoogleDriveService googleDriveService,
                                DuplicateDetectionService duplicateDetectionService,
                                OldFileAnalysisService oldFileAnalysisService,
                                LargeFileAnalysisService largeFileAnalysisService,
                                PermissionAnalysisService permissionAnalysisService) {
        this.googleAccountRepository = googleAccountRepository;
        this.googleDriveService = googleDriveService;
        this.duplicateDetectionService = duplicateDetectionService;
        this.oldFileAnalysisService = oldFileAnalysisService;
        this.largeFileAnalysisService = largeFileAnalysisService;
        this.permissionAnalysisService = permissionAnalysisService;
    }

    /**
     * Periodic background scan job (default 2 AM daily via cron).
     */
    @Scheduled(cron = "${scheduler.scan.cron:0 0 2 * * ?}")
    public void runScheduledScan() {
        if (!schedulerEnabled) {
            log.info("Scheduled scan skipped because scheduler.scan.enabled is false.");
            return;
        }

        List<GoogleAccount> accounts = googleAccountRepository.findAll();
        if (accounts.isEmpty()) {
            log.info("No connected Google accounts found for scheduled scan.");
            return;
        }

        log.info("Starting scheduled scan for {} connected Google account(s)...", accounts.size());

        for (GoogleAccount account : accounts) {
            try {
                log.info("Executing scheduled sync and hygiene analysis for {}", account.getEmail());

                // 1. Sync metadata (incremental if change token exists, otherwise full)
                if (account.getChangeToken() != null && !account.getChangeToken().trim().isEmpty()) {
                    googleDriveService.syncChanges(account.getId());
                } else {
                    googleDriveService.scanAndSyncFiles(account.getId());
                }

                // 2. Execute hygiene analyzers
                duplicateDetectionService.detectDuplicates(account.getId());
                oldFileAnalysisService.analyzeOldFiles(account.getId(), null);
                largeFileAnalysisService.analyzeLargeFiles(account.getId(), null);
                permissionAnalysisService.analyzeExternalShares(account.getId());

                log.info("Scheduled scan and analysis completed successfully for {}", account.getEmail());
            } catch (Exception ex) {
                log.error("Scheduled scan failed for account {}: {}", account.getEmail(), ex.getMessage(), ex);
            }
        }

        log.info("All scheduled scans completed.");
    }
}
