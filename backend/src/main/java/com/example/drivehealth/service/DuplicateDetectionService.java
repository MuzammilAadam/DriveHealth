package com.example.drivehealth.service;

import com.example.drivehealth.dto.DuplicateFileItemResponse;
import com.example.drivehealth.dto.DuplicateGroupResponse;
import com.example.drivehealth.dto.DuplicateResponse;
import com.example.drivehealth.entity.AnalysisFinding;
import com.example.drivehealth.entity.DriveFile;
import com.example.drivehealth.entity.DuplicateGroup;
import com.example.drivehealth.entity.DuplicateGroupFile;
import com.example.drivehealth.entity.FindingType;
import com.example.drivehealth.entity.GoogleAccount;
import com.example.drivehealth.entity.Severity;
import com.example.drivehealth.exception.ResourceNotFoundException;
import com.example.drivehealth.repository.AnalysisFindingRepository;
import com.example.drivehealth.repository.DriveFileRepository;
import com.example.drivehealth.repository.DuplicateGroupRepository;
import com.example.drivehealth.repository.GoogleAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stage 5 & 6: Service for detecting exact duplicate files in Google Drive
 * and populating both DuplicateGroup and the universal AnalysisFinding tables.
 * 
 * ALGORITHM:
 * 1. Read all non-trashed indexed files from MySQL for a given Google Account.
 * 2. Group the files by their MD5 checksum.
 *    (Note: Google Drive provides an MD5 checksum for binary files like PDFs, images,
 *     videos, zips, etc. Native Google Docs/Sheets do not have an MD5 checksum).
 * 3. Any group containing 2 or more files with the exact same MD5 checksum is
 *    identified as an EXACT DUPLICATE (confidence = 100%).
 * 4. Save DuplicateGroup, DuplicateGroupFile, and AnalysisFinding records in MySQL.
 * 5. Return the duplicate groups for user review (NO automatic destructive deletion!).
 */
@Service
public class DuplicateDetectionService {

    private static final Logger log = LoggerFactory.getLogger(DuplicateDetectionService.class);

    private final GoogleAccountRepository googleAccountRepository;
    private final DriveFileRepository driveFileRepository;
    private final DuplicateGroupRepository duplicateGroupRepository;
    private final AnalysisFindingRepository analysisFindingRepository;

    public DuplicateDetectionService(GoogleAccountRepository googleAccountRepository,
                                     DriveFileRepository driveFileRepository,
                                     DuplicateGroupRepository duplicateGroupRepository,
                                     AnalysisFindingRepository analysisFindingRepository) {
        this.googleAccountRepository = googleAccountRepository;
        this.driveFileRepository = driveFileRepository;
        this.duplicateGroupRepository = duplicateGroupRepository;
        this.analysisFindingRepository = analysisFindingRepository;
    }

    /**
     * Executes duplicate detection analysis for an account.
     * Clears previous duplicate groups and findings for this account and performs a fresh analysis.
     */
    @Transactional
    public DuplicateResponse detectDuplicates(Long googleAccountId) {
        GoogleAccount account = resolveAccount(googleAccountId);
        log.info("Starting duplicate detection for Google Account: {}", account.getEmail());

        // Step 1: Fetch all non-trashed indexed files for this account
        List<DriveFile> files = driveFileRepository.findByGoogleAccount_IdAndTrashedFalse(account.getId());

        // Step 2: Group files by MD5 checksum using a straightforward Map
        Map<String, List<DriveFile>> checksumMap = new HashMap<>();
        for (DriveFile file : files) {
            String checksum = file.getMd5Checksum();

            // Skip files without a valid checksum (e.g. native Google Docs, empty files, or folders)
            if (checksum == null || checksum.trim().isEmpty()) {
                continue;
            }

            if (!checksumMap.containsKey(checksum)) {
                checksumMap.put(checksum, new ArrayList<>());
            }
            checksumMap.get(checksum).add(file);
        }

        // Step 3: Delete existing duplicate groups and findings for this account
        duplicateGroupRepository.deleteByGoogleAccountId(account.getId());
        analysisFindingRepository.deleteByGoogleAccountIdAndFindingType(account.getId(), FindingType.DUPLICATE);

        // Step 4: Identify duplicate groups and create findings
        List<DuplicateGroup> groupsToSave = new ArrayList<>();
        List<AnalysisFinding> findingsToSave = new ArrayList<>();
        int totalDuplicateFiles = 0;
        long totalWastedBytes = 0;

        for (Map.Entry<String, List<DriveFile>> entry : checksumMap.entrySet()) {
            List<DriveFile> matchingFiles = entry.getValue();

            // Only 2 or more files sharing the same checksum form a duplicate group
            if (matchingFiles.size() > 1) {
                String checksum = entry.getKey();

                DuplicateGroup group = new DuplicateGroup(
                        account,
                        100, // 100% confidence for exact checksum match
                        "Files have the same checksum",
                        checksum
                );

                for (DriveFile file : matchingFiles) {
                    DuplicateGroupFile groupFile = new DuplicateGroupFile(group, file);
                    group.addGroupFile(groupFile);

                    // Create a universal AnalysisFinding for each duplicate file
                    AnalysisFinding finding = new AnalysisFinding(
                            account,
                            file,
                            FindingType.DUPLICATE,
                            Severity.LOW,
                            100,
                            "File shares identical MD5 checksum (" + checksum + ") with " + (matchingFiles.size() - 1) + " other file(s)"
                    );
                    findingsToSave.add(finding);
                }

                groupsToSave.add(group);
                totalDuplicateFiles += matchingFiles.size();

                // Calculate storage wasted: size of all extra copies (count - 1) * single file size
                Long singleSize = matchingFiles.get(0).getSize();
                if (singleSize != null && singleSize > 0) {
                    totalWastedBytes += (matchingFiles.size() - 1) * singleSize;
                }
            }
        }

        // Step 5: Batch save duplicate groups and analysis findings to MySQL
        if (!groupsToSave.isEmpty()) {
            duplicateGroupRepository.saveAll(groupsToSave);
        }
        if (!findingsToSave.isEmpty()) {
            analysisFindingRepository.saveAll(findingsToSave);
        }

        log.info("Duplicate detection completed for {}. Found {} duplicate groups across {} files.",
                account.getEmail(), groupsToSave.size(), totalDuplicateFiles);

        // Step 6: Map to clean API response
        List<DuplicateGroupResponse> groupResponses = new ArrayList<>();
        for (DuplicateGroup group : groupsToSave) {
            groupResponses.add(mapToGroupResponse(group));
        }

        return new DuplicateResponse(
                groupResponses,
                groupsToSave.size(),
                totalDuplicateFiles,
                totalWastedBytes
        );
    }

    /**
     * Retrieves duplicate analysis results.
     * If no duplicates have been analyzed yet, automatically runs detection.
     */
    @Transactional
    public DuplicateResponse getDuplicates(Long googleAccountId, boolean forceRefresh) {
        GoogleAccount account = resolveAccount(googleAccountId);

        if (forceRefresh) {
            return detectDuplicates(account.getId());
        }

        List<DuplicateGroup> existingGroups = duplicateGroupRepository.findByGoogleAccountIdWithFiles(account.getId());

        if (existingGroups.isEmpty()) {
            // If not analyzed yet, run detection automatically
            return detectDuplicates(account.getId());
        }

        int totalDuplicateFiles = 0;
        long totalWastedBytes = 0;
        List<DuplicateGroupResponse> groupResponses = new ArrayList<>();

        for (DuplicateGroup group : existingGroups) {
            DuplicateGroupResponse response = mapToGroupResponse(group);
            groupResponses.add(response);
            totalDuplicateFiles += response.getFiles().size();
            if (response.getWastedSize() != null) {
                totalWastedBytes += response.getWastedSize();
            }
        }

        return new DuplicateResponse(
                groupResponses,
                existingGroups.size(),
                totalDuplicateFiles,
                totalWastedBytes
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

    /**
     * Maps DuplicateGroup entity to clean response DTO.
     */
    private DuplicateGroupResponse mapToGroupResponse(DuplicateGroup group) {
        List<DuplicateFileItemResponse> fileResponses = new ArrayList<>();
        Long individualFileSize = 0L;

        if (group.getGroupFiles() != null) {
            for (DuplicateGroupFile dgf : group.getGroupFiles()) {
                DriveFile file = dgf.getDriveFile();
                if (file != null) {
                    if (individualFileSize == 0L && file.getSize() != null) {
                        individualFileSize = file.getSize();
                    }

                    fileResponses.add(new DuplicateFileItemResponse(
                            file.getId(),
                            file.getGoogleFileId(),
                            file.getName(),
                            file.getSize(),
                            file.getMimeType(),
                            file.getCreatedTime(),
                            file.getModifiedTime(),
                            file.getWebUrl(),
                            file.getParentId()
                    ));
                }
            }
        }

        Long wastedSize = (fileResponses.size() > 1 && individualFileSize != null)
                ? (fileResponses.size() - 1) * individualFileSize
                : 0L;

        return new DuplicateGroupResponse(
                group.getId(),
                group.getConfidence(),
                group.getReason(),
                group.getMd5Checksum(),
                individualFileSize,
                wastedSize,
                fileResponses
        );
    }
}
