package com.example.drivehealth.service;

import com.example.drivehealth.dto.AnalysisFindingResponse;
import com.example.drivehealth.entity.AnalysisFinding;
import com.example.drivehealth.entity.FindingStatus;
import com.example.drivehealth.entity.FindingType;
import com.example.drivehealth.entity.GoogleAccount;
import com.example.drivehealth.entity.Severity;
import com.example.drivehealth.exception.ResourceNotFoundException;
import com.example.drivehealth.repository.AnalysisFindingRepository;
import com.example.drivehealth.repository.GoogleAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage 6: Service for managing universal hygiene analysis findings.
 * 
 * Serves as the central repository and querying system for all analysis findings
 * (duplicates, old files, large files, external shares, etc.).
 */
@Service
public class AnalysisFindingService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisFindingService.class);

    private final GoogleAccountRepository googleAccountRepository;
    private final AnalysisFindingRepository analysisFindingRepository;

    public AnalysisFindingService(GoogleAccountRepository googleAccountRepository,
                                  AnalysisFindingRepository analysisFindingRepository) {
        this.googleAccountRepository = googleAccountRepository;
        this.analysisFindingRepository = analysisFindingRepository;
    }

    /**
     * Retrieves findings for an account, with optional filters for type, severity, and status.
     */
    public List<AnalysisFindingResponse> getFindings(Long googleAccountId,
                                                    FindingType type,
                                                    Severity severity,
                                                    FindingStatus status) {
        GoogleAccount account = resolveAccount(googleAccountId);

        List<AnalysisFinding> findings;
        if (type == null && severity == null && status == null) {
            findings = analysisFindingRepository.findByGoogleAccountIdWithFile(account.getId());
        } else {
            findings = analysisFindingRepository.findFiltered(account.getId(), type, severity, status);
        }

        List<AnalysisFindingResponse> responses = new ArrayList<>();
        for (AnalysisFinding finding : findings) {
            responses.add(mapToResponse(finding));
        }
        return responses;
    }

    /**
     * Updates finding status (OPEN, IGNORED, RESOLVED).
     * Prepares for Stage 12 decision tracking.
     */
    @Transactional
    public AnalysisFindingResponse updateFindingStatus(Long findingId, FindingStatus newStatus) {
        AnalysisFinding finding = analysisFindingRepository.findById(findingId)
                .orElseThrow(() -> new ResourceNotFoundException("Finding not found with id: " + findingId));

        finding.setStatus(newStatus);
        if (newStatus == FindingStatus.RESOLVED || newStatus == FindingStatus.IGNORED) {
            finding.setResolvedAt(LocalDateTime.now());
        } else {
            finding.setResolvedAt(null);
        }

        AnalysisFinding updated = analysisFindingRepository.save(finding);
        log.info("Updated finding #{} to status {}", findingId, newStatus);
        return mapToResponse(updated);
    }

    /**
     * Saves a list of new findings for an account.
     */
    @Transactional
    public List<AnalysisFinding> saveFindings(List<AnalysisFinding> findings) {
        if (findings == null || findings.isEmpty()) {
            return new ArrayList<>();
        }
        return analysisFindingRepository.saveAll(findings);
    }

    /**
     * Clears findings of a specific type for an account before re-running an analyzer.
     */
    @Transactional
    public void clearFindingsByType(Long googleAccountId, FindingType type) {
        analysisFindingRepository.deleteByGoogleAccountIdAndFindingType(googleAccountId, type);
    }

    /**
     * Resolves GoogleAccount by ID, or defaults to the first connected account.
     */
    public GoogleAccount resolveAccount(Long googleAccountId) {
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
     * Maps an AnalysisFinding entity to an AnalysisFindingResponse DTO.
     */
    public AnalysisFindingResponse mapToResponse(AnalysisFinding finding) {
        String googleFileId = null;
        String fileName = null;
        Long fileId = null;

        if (finding.getDriveFile() != null) {
            fileId = finding.getDriveFile().getId();
            googleFileId = finding.getDriveFile().getGoogleFileId();
            fileName = finding.getDriveFile().getName();
        }

        return new AnalysisFindingResponse(
                finding.getId(),
                finding.getGoogleAccountId(),
                fileId,
                googleFileId,
                fileName,
                finding.getFindingType(),
                finding.getSeverity(),
                finding.getConfidence(),
                finding.getReason(),
                finding.getStatus(),
                finding.getCreatedAt(),
                finding.getResolvedAt()
        );
    }
}
