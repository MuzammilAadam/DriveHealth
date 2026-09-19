package com.example.drivehealth.service;

import com.example.drivehealth.dto.StorageSnapshotResponse;
import com.example.drivehealth.entity.GoogleAccount;
import com.example.drivehealth.entity.StorageSnapshot;
import com.example.drivehealth.exception.ResourceNotFoundException;
import com.example.drivehealth.repository.DriveFileRepository;
import com.example.drivehealth.repository.GoogleAccountRepository;
import com.example.drivehealth.repository.StorageSnapshotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Stage 15: Service for capturing and retrieving storage usage history.
 */
@Service
public class StorageSnapshotService {

    private static final Logger log = LoggerFactory.getLogger(StorageSnapshotService.class);

    private final StorageSnapshotRepository storageSnapshotRepository;
    private final DriveFileRepository driveFileRepository;
    private final GoogleAccountRepository googleAccountRepository;

    public StorageSnapshotService(StorageSnapshotRepository storageSnapshotRepository,
                                  DriveFileRepository driveFileRepository,
                                  GoogleAccountRepository googleAccountRepository) {
        this.storageSnapshotRepository = storageSnapshotRepository;
        this.driveFileRepository = driveFileRepository;
        this.googleAccountRepository = googleAccountRepository;
    }

    /**
     * Captures a point-in-time snapshot of storage metrics for an account.
     */
    @Transactional
    public StorageSnapshot recordSnapshot(GoogleAccount account) {
        Long accountId = account.getId();

        long totalFiles = driveFileRepository.countByGoogleAccountId(accountId);
        long totalStorageBytes = driveFileRepository.sumSizeByGoogleAccountId(accountId);
        long trashedFiles = driveFileRepository.countByGoogleAccountIdAndTrashedTrue(accountId);
        long trashedStorageBytes = driveFileRepository.sumTrashedSizeByGoogleAccountId(accountId);

        long activeFiles = Math.max(0, totalFiles - trashedFiles);
        long activeStorageBytes = Math.max(0, totalStorageBytes - trashedStorageBytes);

        StorageSnapshot snapshot = new StorageSnapshot(
                account,
                totalFiles,
                totalStorageBytes,
                trashedFiles,
                trashedStorageBytes,
                activeFiles,
                activeStorageBytes
        );

        StorageSnapshot saved = storageSnapshotRepository.save(snapshot);
        log.info("Recorded storage snapshot #{} for account {}: totalFiles={}, totalStorageBytes={}",
                saved.getId(), account.getEmail(), totalFiles, totalStorageBytes);
        return saved;
    }

    /**
     * Retrieves historical snapshots for an account.
     */
    @Transactional(readOnly = true)
    public List<StorageSnapshotResponse> getSnapshots(Long googleAccountId) {
        GoogleAccount account = resolveAccount(googleAccountId);
        List<StorageSnapshot> snapshots = storageSnapshotRepository.findByGoogleAccountIdOrderBySnapshotTimeDesc(account.getId());
        List<StorageSnapshotResponse> responses = new ArrayList<>();
        for (StorageSnapshot s : snapshots) {
            responses.add(mapToResponse(s));
        }
        return responses;
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

    private StorageSnapshotResponse mapToResponse(StorageSnapshot s) {
        return new StorageSnapshotResponse(
                s.getId(),
                s.getGoogleAccountId(),
                s.getSnapshotTime(),
                s.getTotalFiles(),
                s.getTotalStorageBytes(),
                s.getTrashedFiles(),
                s.getTrashedStorageBytes(),
                s.getActiveFiles(),
                s.getActiveStorageBytes()
        );
    }
}
