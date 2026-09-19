package com.example.drivehealth.service;

import com.example.drivehealth.dto.ExternalShareItemResponse;
import com.example.drivehealth.dto.ExternalSharesAnalysisResponse;
import com.example.drivehealth.dto.PermissionResponse;
import com.example.drivehealth.entity.AnalysisFinding;
import com.example.drivehealth.entity.DriveFile;
import com.example.drivehealth.entity.FindingType;
import com.example.drivehealth.entity.GoogleAccount;
import com.example.drivehealth.entity.Permission;
import com.example.drivehealth.entity.Severity;
import com.example.drivehealth.exception.ResourceNotFoundException;
import com.example.drivehealth.repository.AnalysisFindingRepository;
import com.example.drivehealth.repository.DriveFileRepository;
import com.example.drivehealth.repository.GoogleAccountRepository;
import com.example.drivehealth.repository.PermissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stage 9: Analyzes permission records to detect risky sharing patterns.
 *
 * Detects:
 *   - EXTERNAL_SHARE: non-owner user/group with a different domain than the account owner
 *   - PUBLIC_FILE: "anyone" type permission (link-shared or fully public)
 *
 * CORE PRINCIPLE:
 * We report facts: who has access, with what role, with or without expiration.
 * We NEVER automatically modify or revoke permissions.
 * The user always makes the final decision.
 */
@Service
public class PermissionAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(PermissionAnalysisService.class);

    private final GoogleAccountRepository googleAccountRepository;
    private final DriveFileRepository driveFileRepository;
    private final PermissionRepository permissionRepository;
    private final AnalysisFindingRepository analysisFindingRepository;
    private final UserRuleService userRuleService;

    public PermissionAnalysisService(GoogleAccountRepository googleAccountRepository,
                                      DriveFileRepository driveFileRepository,
                                      PermissionRepository permissionRepository,
                                      AnalysisFindingRepository analysisFindingRepository,
                                      @org.springframework.beans.factory.annotation.Autowired(required = false) UserRuleService userRuleService) {
        this.googleAccountRepository = googleAccountRepository;
        this.driveFileRepository = driveFileRepository;
        this.permissionRepository = permissionRepository;
        this.analysisFindingRepository = analysisFindingRepository;
        this.userRuleService = userRuleService;
    }

    /**
     * Scans stored permissions and flags files with risky sharing patterns.
     *
     * @param googleAccountId Optional account ID (defaults to first connected account)
     */
    @Transactional
    public ExternalSharesAnalysisResponse analyzeExternalShares(Long googleAccountId) {
        GoogleAccount account = resolveAccount(googleAccountId);
        log.info("Starting external share analysis for account: {}", account.getEmail());

        // Determine the account owner's domain for external share comparison
        String ownerDomain = extractDomain(account.getEmail());

        // Step 1: Clear previous EXTERNAL_SHARE and PUBLIC_FILE findings
        analysisFindingRepository.deleteByGoogleAccountIdAndFindingType(account.getId(), FindingType.EXTERNAL_SHARE);
        analysisFindingRepository.deleteByGoogleAccountIdAndFindingType(account.getId(), FindingType.PUBLIC_FILE);

        // Step 2: Load all permissions for this account
        List<Permission> allPermissions = permissionRepository.findByGoogleAccount_Id(account.getId());

        // Step 3: Group permissions by file ID for efficient processing
        Map<Long, List<Permission>> permsByFile = new HashMap<>();
        for (Permission perm : allPermissions) {
            Long fileId = perm.getDriveFile().getId();
            permsByFile.computeIfAbsent(fileId, k -> new ArrayList<>()).add(perm);
        }

        // Step 4: Load all drive files for this account for metadata lookup
        List<DriveFile> files = driveFileRepository.findByGoogleAccount_IdAndTrashedFalse(account.getId());
        Map<Long, DriveFile> fileById = new HashMap<>();
        for (DriveFile f : files) {
            fileById.put(f.getId(), f);
        }

        List<AnalysisFinding> findingsToSave = new ArrayList<>();
        List<ExternalShareItemResponse> resultItems = new ArrayList<>();
        int externalShareCount = 0;
        int publicFileCount = 0;

        // Step 5: Scan each file's permissions
        for (Map.Entry<Long, List<Permission>> entry : permsByFile.entrySet()) {
            Long fileId = entry.getKey();
            List<Permission> filePerms = entry.getValue();
            DriveFile driveFile = fileById.get(fileId);
            if (driveFile == null) {
                continue;
            }

            // Respect user rules (skip ignored folders or MIME types)
            if (userRuleService != null && (userRuleService.isFolderIgnored(account.getId(), driveFile.getParentId())
                    || userRuleService.isMimeTypeIgnored(account.getId(), driveFile.getMimeType()))) {
                continue;
            }

            List<PermissionResponse> riskyPerms = new ArrayList<>();
            boolean hasExternalShare = false;
            boolean isPublic = false;
            String primaryReason = null;

            for (Permission perm : filePerms) {
                String type = perm.getType();
                String role = perm.getRole();

                if ("owner".equals(role)) {
                    // Skip file owner — not a risk
                    continue;
                }

                if ("anyone".equals(type)) {
                    // File is accessible to anyone with the link (or discoverable)
                    isPublic = true;
                    boolean discoverable = Boolean.TRUE.equals(perm.getAllowFileDiscovery());
                    String permReason = discoverable
                            ? "File is publicly discoverable on the internet with " + role + " access"
                            : "File is accessible to anyone with the link with " + role + " access";
                    if (primaryReason == null) {
                        primaryReason = permReason;
                    }
                    riskyPerms.add(mapPermission(perm));

                } else if ("user".equals(type) || "group".equals(type)) {
                    // Check if email domain differs from account owner domain
                    String granteeEmail = perm.getEmailAddress();
                    if (granteeEmail != null && !granteeEmail.isEmpty()) {
                        String granteeDomain = extractDomain(granteeEmail);
                        if (ownerDomain != null && !ownerDomain.equalsIgnoreCase(granteeDomain)) {
                            hasExternalShare = true;
                            boolean noExpiry = perm.getExpirationTime() == null;
                            String permReason = String.format(
                                    "Shared with external %s %s with %s access%s",
                                    type, granteeEmail, role,
                                    (noExpiry ? " (no expiration date)" : ""));
                            if (primaryReason == null) {
                                primaryReason = permReason;
                            }
                            riskyPerms.add(mapPermission(perm));
                        }
                    }
                }
            }

            if (!riskyPerms.isEmpty()) {
                // Determine severity: writer/owner access is higher risk than reader
                boolean hasWriteAccess = filePerms.stream()
                        .anyMatch(p -> "writer".equals(p.getRole()) || "fileOrganizer".equals(p.getRole()));
                Severity severity = isPublic ? Severity.HIGH
                        : (hasWriteAccess ? Severity.HIGH : Severity.MEDIUM);

                FindingType findingType = isPublic ? FindingType.PUBLIC_FILE : FindingType.EXTERNAL_SHARE;

                AnalysisFinding finding = new AnalysisFinding(
                        account,
                        driveFile,
                        findingType,
                        severity,
                        100,
                        primaryReason != null ? primaryReason : "Risky sharing detected"
                );
                findingsToSave.add(finding);

                resultItems.add(new ExternalShareItemResponse(
                        driveFile.getId(),
                        driveFile.getGoogleFileId(),
                        driveFile.getName(),
                        driveFile.getMimeType(),
                        driveFile.getWebUrl(),
                        riskyPerms,
                        primaryReason
                ));

                if (isPublic) publicFileCount++;
                if (hasExternalShare) externalShareCount++;
            }
        }

        // Step 6: Batch save findings
        if (!findingsToSave.isEmpty()) {
            analysisFindingRepository.saveAll(findingsToSave);
        }

        log.info("External share analysis completed for {}. External: {}, Public: {}, Total risky files: {}",
                account.getEmail(), externalShareCount, publicFileCount, resultItems.size());

        return new ExternalSharesAnalysisResponse(
                resultItems,
                externalShareCount,
                publicFileCount,
                resultItems.size()
        );
    }

    private PermissionResponse mapPermission(Permission perm) {
        return new PermissionResponse(
                perm.getId(),
                perm.getPermissionId(),
                perm.getType(),
                perm.getRole(),
                perm.getEmailAddress(),
                perm.getDisplayName(),
                perm.getDomain(),
                perm.getExpirationTime(),
                perm.getAllowFileDiscovery()
        );
    }

    /**
     * Extracts the domain part from an email (e.g. "user@example.com" → "example.com").
     * Returns null for personal Google accounts (gmail.com) or if email is invalid.
     */
    private String extractDomain(String email) {
        if (email == null || !email.contains("@")) {
            return null;
        }
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
        // Don't flag gmail.com/googlemail.com as "external" — those are personal accounts
        // and not organizational domains
        if ("gmail.com".equals(domain) || "googlemail.com".equals(domain)) {
            return "gmail.com";
        }
        return domain;
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
