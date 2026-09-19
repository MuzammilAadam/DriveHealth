package com.example.drivehealth.service;

import com.example.drivehealth.dto.DashboardSummaryResponse;
import com.example.drivehealth.entity.FindingStatus;
import com.example.drivehealth.entity.FindingType;
import com.example.drivehealth.entity.GoogleAccount;
import com.example.drivehealth.entity.Severity;
import com.example.drivehealth.exception.ResourceNotFoundException;
import com.example.drivehealth.repository.AnalysisFindingRepository;
import com.example.drivehealth.repository.DriveFileRepository;
import com.example.drivehealth.repository.DuplicateGroupFileRepository;
import com.example.drivehealth.repository.DuplicateGroupRepository;
import com.example.drivehealth.repository.GoogleAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Stage 10: Service providing aggregated hygiene dashboard statistics.
 */
@Service
public class DashboardService {

    private final GoogleAccountRepository googleAccountRepository;
    private final DriveFileRepository driveFileRepository;
    private final AnalysisFindingRepository analysisFindingRepository;
    private final DuplicateGroupRepository duplicateGroupRepository;
    private final DuplicateGroupFileRepository duplicateGroupFileRepository;

    public DashboardService(GoogleAccountRepository googleAccountRepository,
                            DriveFileRepository driveFileRepository,
                            AnalysisFindingRepository analysisFindingRepository,
                            DuplicateGroupRepository duplicateGroupRepository,
                            DuplicateGroupFileRepository duplicateGroupFileRepository) {
        this.googleAccountRepository = googleAccountRepository;
        this.driveFileRepository = driveFileRepository;
        this.analysisFindingRepository = analysisFindingRepository;
        this.duplicateGroupRepository = duplicateGroupRepository;
        this.duplicateGroupFileRepository = duplicateGroupFileRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(Long googleAccountId) {
        GoogleAccount account = resolveAccount(googleAccountId);
        Long accountId = account.getId();

        // 1. Storage & file metrics
        long totalFiles = driveFileRepository.countByGoogleAccountId(accountId);
        long totalStorageBytes = driveFileRepository.sumSizeByGoogleAccountId(accountId);
        long trashedFiles = driveFileRepository.countByGoogleAccountIdAndTrashedTrue(accountId);

        // 2. Findings overall status metrics
        long totalFindings = analysisFindingRepository.countByGoogleAccount_Id(accountId);
        long openFindings = analysisFindingRepository.countByGoogleAccount_IdAndStatus(accountId, FindingStatus.OPEN);
        long ignoredFindings = analysisFindingRepository.countByGoogleAccount_IdAndStatus(accountId, FindingStatus.IGNORED);
        long resolvedFindings = analysisFindingRepository.countByGoogleAccount_IdAndStatus(accountId, FindingStatus.RESOLVED);
        long highSeverityOpenFindings = analysisFindingRepository.countByGoogleAccount_IdAndStatusAndSeverity(
                accountId, FindingStatus.OPEN, Severity.HIGH);

        // 3. Category metrics
        long duplicateGroups = duplicateGroupRepository.countByGoogleAccount_Id(accountId);
        long duplicateFiles = duplicateGroupFileRepository.countByDuplicateGroup_GoogleAccount_Id(accountId);
        long oldFiles = analysisFindingRepository.countByGoogleAccount_IdAndFindingType(accountId, FindingType.OLD_FILE);
        long largeFiles = analysisFindingRepository.countByGoogleAccount_IdAndFindingType(accountId, FindingType.LARGE_FILE);
        long externalShares = analysisFindingRepository.countByGoogleAccount_IdAndFindingType(accountId, FindingType.EXTERNAL_SHARE);
        long publicFiles = analysisFindingRepository.countByGoogleAccount_IdAndFindingType(accountId, FindingType.PUBLIC_FILE);

        return new DashboardSummaryResponse(
                accountId,
                account.getEmail(),
                totalFiles,
                totalStorageBytes,
                trashedFiles,
                totalFindings,
                openFindings,
                ignoredFindings,
                resolvedFindings,
                highSeverityOpenFindings,
                duplicateGroups,
                duplicateFiles,
                oldFiles,
                largeFiles,
                externalShares,
                publicFiles
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
