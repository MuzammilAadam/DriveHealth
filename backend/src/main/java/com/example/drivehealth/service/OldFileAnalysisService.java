package com.example.drivehealth.service;

import com.example.drivehealth.dto.OldFileItemResponse;
import com.example.drivehealth.dto.OldFilesAnalysisResponse;
import com.example.drivehealth.entity.AnalysisFinding;
import com.example.drivehealth.entity.DriveFile;
import com.example.drivehealth.entity.FindingType;
import com.example.drivehealth.entity.GoogleAccount;
import com.example.drivehealth.entity.Severity;
import com.example.drivehealth.exception.ResourceNotFoundException;
import com.example.drivehealth.repository.AnalysisFindingRepository;
import com.example.drivehealth.repository.DriveFileRepository;
import com.example.drivehealth.repository.GoogleAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage 7: Service for analyzing old, untouched files in Google Drive.
 * 
 * CORE PRINCIPLE:
 * We identify files that haven't been modified for a long time (default > 2 years).
 * We NEVER assume or recommend that an old file should be deleted automatically.
 * The explanation clearly states facts: "Not modified for more than 2 years."
 * The user always makes the final decision.
 */
@Service
public class OldFileAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(OldFileAnalysisService.class);

    @Value("${analysis.old-file.years:2}")
    private int defaultYearsThreshold;

    private final GoogleAccountRepository googleAccountRepository;
    private final DriveFileRepository driveFileRepository;
    private final AnalysisFindingRepository analysisFindingRepository;

    public OldFileAnalysisService(GoogleAccountRepository googleAccountRepository,
                                  DriveFileRepository driveFileRepository,
                                  AnalysisFindingRepository analysisFindingRepository) {
        this.googleAccountRepository = googleAccountRepository;
        this.driveFileRepository = driveFileRepository;
        this.analysisFindingRepository = analysisFindingRepository;
    }

    /**
     * Analyzes and flags files that have not been modified for more than the specified threshold.
     * Generates universal AnalysisFinding records with type OLD_FILE.
     * 
     * @param googleAccountId Optional specific account ID (defaults to first account)
     * @param years Optional custom year threshold (defaults to configured 2 years)
     */
    @Transactional
    public OldFilesAnalysisResponse analyzeOldFiles(Long googleAccountId, Integer years) {
        GoogleAccount account = resolveAccount(googleAccountId);
        int thresholdYears = (years != null && years > 0) ? years : defaultYearsThreshold;
        LocalDateTime cutoffDate = LocalDateTime.now().minusYears(thresholdYears);

        log.info("Starting old file analysis for {} with threshold: {} years (cutoff: {})",
                account.getEmail(), thresholdYears, cutoffDate);

        // Step 1: Fetch all non-trashed indexed files for this account
        List<DriveFile> files = driveFileRepository.findByGoogleAccount_IdAndTrashedFalse(account.getId());

        // Step 2: Clear previous OLD_FILE findings for this account
        analysisFindingRepository.deleteByGoogleAccountIdAndFindingType(account.getId(), FindingType.OLD_FILE);

        List<AnalysisFinding> findingsToSave = new ArrayList<>();
        List<OldFileItemResponse> oldFilesList = new ArrayList<>();
        long totalOldStorageBytes = 0;

        // Step 3: Scan each file using straightforward loop logic
        for (DriveFile file : files) {
            LocalDateTime lastModified = file.getModifiedTime();
            if (lastModified == null) {
                lastModified = file.getCreatedTime();
            }

            // Skip if no timestamp is available
            if (lastModified == null) {
                continue;
            }

            // Check if last modified timestamp is older than cutoff date
            if (lastModified.isBefore(cutoffDate)) {
                long daysSinceModified = ChronoUnit.DAYS.between(lastModified, LocalDateTime.now());
                long yearsOld = daysSinceModified / 365;

                // Human-readable explanation based on actual facts
                String reason = "Not modified for more than " + thresholdYears + " years";

                // Older files have higher severity (>= 5 years: HIGH, otherwise MEDIUM)
                Severity severity = (yearsOld >= 5) ? Severity.HIGH : Severity.MEDIUM;

                AnalysisFinding finding = new AnalysisFinding(
                        account,
                        file,
                        FindingType.OLD_FILE,
                        severity,
                        100,
                        reason
                );
                findingsToSave.add(finding);

                long size = (file.getSize() != null) ? file.getSize() : 0L;
                totalOldStorageBytes += size;

                oldFilesList.add(new OldFileItemResponse(
                        file.getId(),
                        file.getGoogleFileId(),
                        file.getName(),
                        file.getMimeType(),
                        size,
                        file.getCreatedTime(),
                        file.getModifiedTime(),
                        daysSinceModified,
                        file.getWebUrl(),
                        file.getParentId(),
                        reason
                ));
            }
        }

        // Step 4: Batch save findings to MySQL
        if (!findingsToSave.isEmpty()) {
            analysisFindingRepository.saveAll(findingsToSave);
        }

        log.info("Old file analysis completed for {}. Found {} old files consuming {} bytes.",
                account.getEmail(), oldFilesList.size(), totalOldStorageBytes);

        return new OldFilesAnalysisResponse(
                oldFilesList,
                oldFilesList.size(),
                totalOldStorageBytes,
                thresholdYears,
                cutoffDate
        );
    }

    /**
     * Resolves GoogleAccount by ID, or defaults to the first connected account.
     */
    private GoogleAccount resolveAccount(Long googleAccountId) {
        if (googleAccountId != null) {
            return googleAccountRepository.findById(googleAccountId)
                    .orElseThrow(() -> new ResourceNotFoundException("Google account not found with id: " + googleAccountId));
        }

        List<GoogleAccount> accounts = googleAccountRepository.findAll();
        if (accounts.isEmpty()) {
            throw new ResourceNotFoundException("No connected Google account found. Please connect your Google Drive first.");
        }
        return accounts.get(0);
    }
}
