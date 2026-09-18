package com.example.drivehealth.service;

import com.example.drivehealth.dto.LargeFileAnalysisResponse;
import com.example.drivehealth.dto.LargeFileItemResponse;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Stage 8: Detects files that exceed a configurable size threshold.
 *
 * CORE PRINCIPLE:
 * We report the factual file size and compare it to the threshold.
 * We never suggest the user must delete a file — that is always the user's decision.
 * Severity scales with size:
 *   - >= 5 GB  → CRITICAL
 *   - >= 1 GB  → HIGH
 *   - >= threshold (default 500 MB) → MEDIUM
 */
@Service
public class LargeFileAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(LargeFileAnalysisService.class);

    // 500 MB default — configurable via application.properties
    @Value("${analysis.large-file.bytes:524288000}")
    private long defaultThresholdBytes;

    private static final long ONE_GB = 1_073_741_824L;
    private static final long FIVE_GB = 5_368_709_120L;

    private final GoogleAccountRepository googleAccountRepository;
    private final DriveFileRepository driveFileRepository;
    private final AnalysisFindingRepository analysisFindingRepository;

    public LargeFileAnalysisService(GoogleAccountRepository googleAccountRepository,
                                     DriveFileRepository driveFileRepository,
                                     AnalysisFindingRepository analysisFindingRepository) {
        this.googleAccountRepository = googleAccountRepository;
        this.driveFileRepository = driveFileRepository;
        this.analysisFindingRepository = analysisFindingRepository;
    }

    /**
     * Scans indexed DriveFile records and flags any that exceed the threshold.
     *
     * @param googleAccountId Optional account ID (defaults to first connected account)
     * @param thresholdBytes  Optional custom threshold in bytes (defaults to configured value)
     */
    @Transactional
    public LargeFileAnalysisResponse analyzeLargeFiles(Long googleAccountId, Long thresholdBytes) {
        GoogleAccount account = resolveAccount(googleAccountId);
        long threshold = (thresholdBytes != null && thresholdBytes > 0) ? thresholdBytes : defaultThresholdBytes;
        double thresholdMb = threshold / (1024.0 * 1024.0);

        log.info("Starting large file analysis for {} with threshold: {} bytes ({} MB)",
                account.getEmail(), threshold, String.format("%.1f", thresholdMb));

        // Step 1: Fetch all non-trashed indexed files for this account
        List<DriveFile> files = driveFileRepository.findByGoogleAccount_IdAndTrashedFalse(account.getId());

        // Step 2: Clear previous LARGE_FILE findings for this account
        analysisFindingRepository.deleteByGoogleAccountIdAndFindingType(account.getId(), FindingType.LARGE_FILE);

        List<AnalysisFinding> findingsToSave = new ArrayList<>();
        List<LargeFileItemResponse> largeFilesList = new ArrayList<>();
        long totalLargeStorageBytes = 0;

        // Step 3: Scan each file
        for (DriveFile file : files) {
            Long size = file.getSize();
            if (size == null || size <= threshold) {
                continue;
            }

            // Scale severity based on actual size
            Severity severity;
            if (size >= FIVE_GB) {
                severity = Severity.CRITICAL;
            } else if (size >= ONE_GB) {
                severity = Severity.HIGH;
            } else {
                severity = Severity.MEDIUM;
            }

            // Factual, human-readable reason
            double fileMb = size / (1024.0 * 1024.0);
            String reason = String.format("File size is %.1f MB, exceeding the %.0f MB threshold",
                    fileMb, thresholdMb);

            AnalysisFinding finding = new AnalysisFinding(
                    account,
                    file,
                    FindingType.LARGE_FILE,
                    severity,
                    100,
                    reason
            );
            findingsToSave.add(finding);
            totalLargeStorageBytes += size;

            largeFilesList.add(new LargeFileItemResponse(
                    file.getId(),
                    file.getGoogleFileId(),
                    file.getName(),
                    file.getMimeType(),
                    size,
                    file.getCreatedTime(),
                    file.getModifiedTime(),
                    file.getWebUrl(),
                    file.getParentId(),
                    reason
            ));
        }

        // Step 4: Batch save findings
        if (!findingsToSave.isEmpty()) {
            analysisFindingRepository.saveAll(findingsToSave);
        }

        log.info("Large file analysis completed for {}. Found {} large files consuming {} bytes.",
                account.getEmail(), largeFilesList.size(), totalLargeStorageBytes);

        return new LargeFileAnalysisResponse(
                largeFilesList,
                largeFilesList.size(),
                totalLargeStorageBytes,
                threshold
        );
    }

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
