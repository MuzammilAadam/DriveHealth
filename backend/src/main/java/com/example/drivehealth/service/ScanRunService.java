package com.example.drivehealth.service;

import com.example.drivehealth.dto.ScanRunResponse;
import com.example.drivehealth.entity.GoogleAccount;
import com.example.drivehealth.entity.ScanRun;
import com.example.drivehealth.exception.ResourceNotFoundException;
import com.example.drivehealth.repository.GoogleAccountRepository;
import com.example.drivehealth.repository.ScanRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage 14: Service for tracking scan runs and execution history.
 */
@Service
public class ScanRunService {

    private static final Logger log = LoggerFactory.getLogger(ScanRunService.class);

    private final ScanRunRepository scanRunRepository;
    private final GoogleAccountRepository googleAccountRepository;

    public ScanRunService(ScanRunRepository scanRunRepository,
                          GoogleAccountRepository googleAccountRepository) {
        this.scanRunRepository = scanRunRepository;
        this.googleAccountRepository = googleAccountRepository;
    }

    /**
     * Starts a new scan run record in IN_PROGRESS status.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ScanRun startScanRun(GoogleAccount account, String scanType) {
        ScanRun run = new ScanRun(account, scanType);
        ScanRun saved = scanRunRepository.save(run);
        log.info("Started {} scan #{} for account {}", scanType, saved.getId(), account.getEmail());
        return saved;
    }

    /**
     * Completes an existing scan run record with success stats.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeScanRun(ScanRun scanRun, int filesScanned, int newFiles, int updatedFiles, int findingsCreated) {
        scanRun.setStatus("COMPLETED");
        scanRun.setCompletedAt(LocalDateTime.now());
        scanRun.setFilesScanned(filesScanned);
        scanRun.setNewFiles(newFiles);
        scanRun.setUpdatedFiles(updatedFiles);
        scanRun.setFindingsCreated(findingsCreated);
        scanRunRepository.save(scanRun);
        log.info("Completed scan #{} for account {}: scanned={}, new={}, updated={}, findings={}",
                scanRun.getId(), scanRun.getGoogleAccount().getEmail(), filesScanned, newFiles, updatedFiles, findingsCreated);
    }

    /**
     * Records a failed scan run with error information.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failScanRun(ScanRun scanRun, String errorMessage) {
        scanRun.setStatus("FAILED");
        scanRun.setCompletedAt(LocalDateTime.now());
        if (errorMessage != null && errorMessage.length() > 1000) {
            errorMessage = errorMessage.substring(0, 997) + "...";
        }
        scanRun.setErrorMessage(errorMessage);
        scanRunRepository.save(scanRun);
        log.error("Scan #{} failed for account {}: {}",
                scanRun.getId(), scanRun.getGoogleAccount().getEmail(), errorMessage);
    }

    /**
     * Retrieves scan history for an account or all accounts.
     */
    @Transactional(readOnly = true)
    public List<ScanRunResponse> getScans(Long googleAccountId) {
        List<ScanRun> runs;
        if (googleAccountId != null) {
            runs = scanRunRepository.findByGoogleAccountIdOrderByStartedAtDesc(googleAccountId);
        } else {
            runs = scanRunRepository.findAllByOrderByStartedAtDesc();
        }

        List<ScanRunResponse> responses = new ArrayList<>();
        for (ScanRun run : runs) {
            responses.add(mapToResponse(run));
        }
        return responses;
    }

    private ScanRunResponse mapToResponse(ScanRun run) {
        return new ScanRunResponse(
                run.getId(),
                run.getGoogleAccountId(),
                run.getScanType(),
                run.getStatus(),
                run.getStartedAt(),
                run.getCompletedAt(),
                run.getFilesScanned(),
                run.getNewFiles(),
                run.getUpdatedFiles(),
                run.getFindingsCreated(),
                run.getErrorMessage()
        );
    }
}
