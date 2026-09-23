package com.example.drivehealth.service;

import com.example.drivehealth.dto.DriveFileDto;
import com.example.drivehealth.dto.DriveFileResponse;
import com.example.drivehealth.dto.DriveFilesPageResponse;
import com.example.drivehealth.dto.ScanSummaryResponse;
import com.example.drivehealth.dto.StorageQuotaResponse;
import com.example.drivehealth.dto.google.GoogleDriveAboutResponse;
import com.example.drivehealth.dto.google.GoogleDriveChangeItem;
import com.example.drivehealth.dto.google.GoogleDriveChangeListResponse;
import com.example.drivehealth.dto.google.GoogleDriveFileItem;
import com.example.drivehealth.dto.google.GoogleDriveFileListResponse;
import com.example.drivehealth.dto.google.GoogleDrivePermission;
import com.example.drivehealth.dto.google.GoogleDriveStartPageTokenResponse;
import com.example.drivehealth.entity.DriveFile;
import com.example.drivehealth.entity.GoogleAccount;
import com.example.drivehealth.entity.Permission;
import com.example.drivehealth.entity.ScanRun;
import com.example.drivehealth.exception.OAuthException;
import com.example.drivehealth.exception.ResourceNotFoundException;
import com.example.drivehealth.repository.DriveFileRepository;
import com.example.drivehealth.repository.GoogleAccountRepository;
import com.example.drivehealth.repository.PermissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service for interacting with Google Drive API v3 and managing local Drive metadata.
 * 
 * CORE PRINCIPLE:
 * We retrieve and store file metadata (names, sizes, dates, MD5 checksums).
 * We NEVER download or store user file contents (documents, images, videos).
 */
@Service
public class GoogleDriveService {

    private static final Logger log = LoggerFactory.getLogger(GoogleDriveService.class);

    private static final String DRIVE_FILES_API_URL = "https://www.googleapis.com/drive/v3/files";
    private static final String DRIVE_ABOUT_API_URL = "https://www.googleapis.com/drive/v3/about";
    private static final String DRIVE_CHANGES_API_URL = "https://www.googleapis.com/drive/v3/changes";
    private static final String DRIVE_START_PAGE_TOKEN_URL = "https://www.googleapis.com/drive/v3/changes/startPageToken";

    // Request file metadata fields + permissions without spaces to ensure clean URI encoding
    private static final String FIELDS_QUERY = "nextPageToken,files(id,name,mimeType,size,createdTime,modifiedTime,parents,webViewLink,md5Checksum,trashed,owners,permissions)";
    private static final String CHANGES_FIELDS_QUERY = "nextPageToken,newStartPageToken,changes(fileId,removed,file(id,name,mimeType,size,createdTime,modifiedTime,parents,webViewLink,md5Checksum,trashed,owners,permissions))";

    private final GoogleAccountRepository googleAccountRepository;
    private final DriveFileRepository driveFileRepository;
    private final GoogleOAuthService googleOAuthService;
    private final RestTemplate restTemplate;
    private final PermissionRepository permissionRepository;
    private final ScanRunService scanRunService;
    private final StorageSnapshotService storageSnapshotService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final OldFileAnalysisService oldFileAnalysisService;
    private final LargeFileAnalysisService largeFileAnalysisService;
    private final PermissionAnalysisService permissionAnalysisService;

    public GoogleDriveService(GoogleAccountRepository googleAccountRepository,
                              DriveFileRepository driveFileRepository,
                              GoogleOAuthService googleOAuthService,
                              RestTemplate restTemplate,
                              PermissionRepository permissionRepository,
                              ScanRunService scanRunService,
                              StorageSnapshotService storageSnapshotService,
                              @Lazy DuplicateDetectionService duplicateDetectionService,
                              @Lazy OldFileAnalysisService oldFileAnalysisService,
                              @Lazy LargeFileAnalysisService largeFileAnalysisService,
                              @Lazy PermissionAnalysisService permissionAnalysisService) {
        this.googleAccountRepository = googleAccountRepository;
        this.driveFileRepository = driveFileRepository;
        this.googleOAuthService = googleOAuthService;
        this.restTemplate = restTemplate;
        this.permissionRepository = permissionRepository;
        this.scanRunService = scanRunService;
        this.storageSnapshotService = storageSnapshotService;
        this.duplicateDetectionService = duplicateDetectionService;
        this.oldFileAnalysisService = oldFileAnalysisService;
        this.largeFileAnalysisService = largeFileAnalysisService;
        this.permissionAnalysisService = permissionAnalysisService;
    }

    /**
     * Retrieves a single page of files directly from Google Drive API.
     */
    public DriveFilesPageResponse getFilesPage(Long googleAccountId, int pageSize, String pageToken) {
        GoogleAccount account = getAccount(googleAccountId);
        String accessToken = googleOAuthService.getValidAccessToken(account);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(DRIVE_FILES_API_URL)
                .queryParam("pageSize", Math.min(pageSize, 1000))
                .queryParam("fields", FIELDS_QUERY)
                .queryParam("supportsAllDrives", true)
                .queryParam("includeItemsFromAllDrives", true);

        if (pageToken != null && !pageToken.trim().isEmpty()) {
            uriBuilder.queryParam("pageToken", pageToken);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            URI requestUri = uriBuilder.build().encode().toUri();
            ResponseEntity<GoogleDriveFileListResponse> response = restTemplate.exchange(
                    requestUri,
                    HttpMethod.GET,
                    request,
                    GoogleDriveFileListResponse.class
            );

            GoogleDriveFileListResponse body = response.getBody();
            if (body == null || body.getFiles() == null) {
                return new DriveFilesPageResponse(new ArrayList<>(), null);
            }

            List<DriveFileDto> dtoList = new ArrayList<>();
            for (GoogleDriveFileItem item : body.getFiles()) {
                dtoList.add(mapToDriveFileDto(item));
            }

            return new DriveFilesPageResponse(dtoList, body.getNextPageToken());
        } catch (Exception ex) {
            throw translateGoogleDriveException(ex, account);
        }
    }

    /**
     * Retrieves ALL files from Google Drive by continuously following 'nextPageToken'.
     * Returns raw GoogleDriveFileItem objects (which include permissions).
     */
    public List<GoogleDriveFileItem> fetchAllFileItems(Long googleAccountId) {
        GoogleAccount account = getAccount(googleAccountId);
        String accessToken = googleOAuthService.getValidAccessToken(account);

        List<GoogleDriveFileItem> allItems = new ArrayList<>();
        String pageToken = null;

        log.info("Starting full metadata retrieval for Google Account ID: {}", googleAccountId);

        do {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(DRIVE_FILES_API_URL)
                    .queryParam("pageSize", 100)
                    .queryParam("fields", FIELDS_QUERY)
                    .queryParam("supportsAllDrives", true)
                    .queryParam("includeItemsFromAllDrives", true);
            if (pageToken != null && !pageToken.trim().isEmpty()) {
                uriBuilder.queryParam("pageToken", pageToken);
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<GoogleDriveFileListResponse> response;
            try {
                URI requestUri = uriBuilder.build().encode().toUri();
                response = restTemplate.exchange(
                        requestUri, HttpMethod.GET, request, GoogleDriveFileListResponse.class);
            } catch (Exception ex) {
                throw translateGoogleDriveException(ex, account);
            }

            GoogleDriveFileListResponse body = response.getBody();
            if (body != null && body.getFiles() != null) {
                allItems.addAll(body.getFiles());
            }
            pageToken = (body != null) ? body.getNextPageToken() : null;
        } while (pageToken != null && !pageToken.trim().isEmpty());

        log.info("Finished metadata retrieval for Google Account ID: {}. Total items: {}", googleAccountId, allItems.size());
        return allItems;
    }

    /**
     * Retrieves ALL files from Google Drive as DTOs.
     */
    public List<DriveFileDto> fetchAllFiles(Long googleAccountId) {
        List<GoogleDriveFileItem> items = fetchAllFileItems(googleAccountId);
        List<DriveFileDto> dtos = new ArrayList<>();
        for (GoogleDriveFileItem item : items) {
            dtos.add(mapToDriveFileDto(item));
        }
        return dtos;
    }

    /**
     * Stage 4 + 9: Scan and sync metadata into local MySQL database, including permissions.
     */
    @Transactional
    public ScanSummaryResponse scanAndSyncFiles(Long googleAccountId) {
        GoogleAccount account = getAccount(googleAccountId);
        log.info("Starting scan and sync for Google Account: {}", account.getEmail());

        ScanRun scanRun = scanRunService.startScanRun(account, "FULL");

        try {
            // Step 1: Fetch raw items (includes permissions)
            List<GoogleDriveFileItem> rawItems = fetchAllFileItems(account.getId());
            int filesScanned = rawItems.size();

            // Step 2: Load existing local records into a Map for fast O(1) matching
            List<DriveFile> existingFiles = driveFileRepository.findByGoogleAccount_Id(account.getId());
            Map<String, DriveFile> existingFileMap = new HashMap<>();
            for (DriveFile file : existingFiles) {
                existingFileMap.put(file.getGoogleFileId(), file);
            }

            int newFiles = 0;
            int updatedFiles = 0;
            LocalDateTime now = LocalDateTime.now();
            List<DriveFile> recordsToSave = new ArrayList<>();

            // Step 3: Process every scanned file
            for (GoogleDriveFileItem item : rawItems) {
                DriveFileDto dto = mapToDriveFileDto(item);
                DriveFile existingFile = existingFileMap.get(dto.getId());

                if (existingFile != null) {
                    existingFile.setName(dto.getName());
                    existingFile.setMimeType(dto.getMimeType());
                    existingFile.setSize(dto.getSize() != null ? dto.getSize() : 0L);
                    existingFile.setCreatedTime(dto.getCreatedTime());
                    existingFile.setModifiedTime(dto.getModifiedTime());
                    existingFile.setParentId(dto.getParentId());
                    existingFile.setWebUrl(dto.getWebUrl());
                    existingFile.setMd5Checksum(dto.getMd5Checksum());
                    existingFile.setTrashed(dto.getTrashed() != null ? dto.getTrashed() : false);
                    existingFile.setOwnerEmail(dto.getOwnerEmail());
                    existingFile.setIndexedAt(now);
                    recordsToSave.add(existingFile);
                    updatedFiles++;
                } else {
                    DriveFile newFile = new DriveFile();
                    newFile.setGoogleAccount(account);
                    newFile.setGoogleFileId(dto.getId());
                    newFile.setName(dto.getName());
                    newFile.setMimeType(dto.getMimeType());
                    newFile.setSize(dto.getSize() != null ? dto.getSize() : 0L);
                    newFile.setCreatedTime(dto.getCreatedTime());
                    newFile.setModifiedTime(dto.getModifiedTime());
                    newFile.setParentId(dto.getParentId());
                    newFile.setWebUrl(dto.getWebUrl());
                    newFile.setMd5Checksum(dto.getMd5Checksum());
                    newFile.setTrashed(dto.getTrashed() != null ? dto.getTrashed() : false);
                    newFile.setOwnerEmail(dto.getOwnerEmail());
                    newFile.setIndexedAt(now);
                    recordsToSave.add(newFile);
                    newFiles++;
                }
            }

            // Step 4: Batch save files
            if (!recordsToSave.isEmpty()) {
                driveFileRepository.saveAll(recordsToSave);
            }

            // Step 5: Sync permissions safely
            List<DriveFile> allSavedFiles = driveFileRepository.findByGoogleAccount_Id(account.getId());
            Map<String, DriveFile> savedFileMap = new HashMap<>();
            for (DriveFile f : allSavedFiles) {
                savedFileMap.put(f.getGoogleFileId(), f);
            }
            syncPermissionsFromItems(account, rawItems, savedFileMap);

            // Step 6: Initialize start page token for future incremental change tracking
            String startPageToken = fetchStartPageToken(account);
            if (startPageToken != null) {
                account.setChangeToken(startPageToken);
            }

            // Step 7: Update account's lastSyncedAt
            account.setLastSyncedAt(now);
            googleAccountRepository.save(account);

            // Step 8: Sync actual storage quota from Google Drive API
            syncStorageQuota(account);

            // Step 9: Record storage snapshot
            storageSnapshotService.recordSnapshot(account);

            // Step 10: Run hygiene analyzers to populate findings
            int findingsCreated = 0;
            try {
                var dupResult = duplicateDetectionService.detectDuplicates(account.getId());
                if (dupResult != null) findingsCreated += dupResult.getTotalDuplicateFiles();
                oldFileAnalysisService.analyzeOldFiles(account.getId(), null);
                largeFileAnalysisService.analyzeLargeFiles(account.getId(), null);
                permissionAnalysisService.analyzeExternalShares(account.getId());
            } catch (Exception ex) {
                log.warn("Hygiene analysis post-scan had non-fatal warning for account {}: {}", account.getEmail(), ex.getMessage());
            }

            // Step 11: Complete scan run
            scanRunService.completeScanRun(scanRun, filesScanned, newFiles, updatedFiles, findingsCreated);

            log.info("Scan completed for {}: scanned={}, new={}, updated={}",
                    account.getEmail(), filesScanned, newFiles, updatedFiles);

            return new ScanSummaryResponse(filesScanned, newFiles, updatedFiles);
        } catch (Exception ex) {
            scanRunService.failScanRun(scanRun, ex.getMessage());
            log.error("Scan failed for account {}: {}", account.getEmail(), ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Stage 17: Performs incremental change synchronization using Google Drive Changes API.
     * If no change token exists yet or token is expired, gracefully performs a full scan.
     */
    @Transactional
    public ScanSummaryResponse syncChanges(Long googleAccountId) {
        GoogleAccount account = getAccount(googleAccountId);

        if (account.getChangeToken() == null || account.getChangeToken().trim().isEmpty()) {
            log.info("No change token found for account {}. Performing initial full scan.", account.getEmail());
            return scanAndSyncFiles(googleAccountId);
        }

        log.info("Starting incremental change sync for Google Account: {}", account.getEmail());
        ScanRun scanRun = scanRunService.startScanRun(account, "INCREMENTAL");

        String pageToken = account.getChangeToken();
        String newStartPageToken = null;
        int changesProcessed = 0;
        int newFiles = 0;
        int updatedFiles = 0;
        LocalDateTime now = LocalDateTime.now();

        try {
            String accessToken = googleOAuthService.getValidAccessToken(account);

            do {
                UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(DRIVE_CHANGES_API_URL)
                        .queryParam("pageToken", pageToken)
                        .queryParam("fields", CHANGES_FIELDS_QUERY)
                        .queryParam("supportsAllDrives", true)
                        .queryParam("includeItemsFromAllDrives", true);

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(accessToken);
                HttpEntity<Void> request = new HttpEntity<>(headers);

                URI requestUri = uriBuilder.build().encode().toUri();
                ResponseEntity<GoogleDriveChangeListResponse> response = restTemplate.exchange(
                        requestUri, HttpMethod.GET, request,
                        GoogleDriveChangeListResponse.class
                );

                GoogleDriveChangeListResponse body = response.getBody();
                if (body == null || body.getChanges() == null) {
                    break;
                }

                for (GoogleDriveChangeItem change : body.getChanges()) {
                    changesProcessed++;
                    String fileId = change.getFileId();

                    if (Boolean.TRUE.equals(change.getRemoved())) {
                        driveFileRepository.deleteByGoogleAccount_IdAndGoogleFileId(account.getId(), fileId);
                        updatedFiles++;
                    } else if (change.getFile() != null) {
                        GoogleDriveFileItem gFile = change.getFile();
                        DriveFile existing = driveFileRepository.findByGoogleAccount_IdAndGoogleFileId(account.getId(), fileId).orElse(null);

                        DriveFileDto dto = mapToDriveFileDto(gFile);
                        if (existing != null) {
                            existing.setName(dto.getName());
                            existing.setMimeType(dto.getMimeType());
                            existing.setSize(dto.getSize() != null ? dto.getSize() : 0L);
                            existing.setCreatedTime(dto.getCreatedTime());
                            existing.setModifiedTime(dto.getModifiedTime());
                            existing.setParentId(dto.getParentId());
                            existing.setWebUrl(dto.getWebUrl());
                            existing.setMd5Checksum(dto.getMd5Checksum());
                            existing.setTrashed(dto.getTrashed() != null ? dto.getTrashed() : false);
                            existing.setOwnerEmail(dto.getOwnerEmail());
                            existing.setIndexedAt(now);
                            driveFileRepository.save(existing);
                            syncPermissionsForSingleFile(account, existing, gFile.getPermissions());
                            updatedFiles++;
                        } else {
                            DriveFile newFile = new DriveFile();
                            newFile.setGoogleAccount(account);
                            newFile.setGoogleFileId(dto.getId());
                            newFile.setName(dto.getName());
                            newFile.setMimeType(dto.getMimeType());
                            newFile.setSize(dto.getSize() != null ? dto.getSize() : 0L);
                            newFile.setCreatedTime(dto.getCreatedTime());
                            newFile.setModifiedTime(dto.getModifiedTime());
                            newFile.setParentId(dto.getParentId());
                            newFile.setWebUrl(dto.getWebUrl());
                            newFile.setMd5Checksum(dto.getMd5Checksum());
                            newFile.setTrashed(dto.getTrashed() != null ? dto.getTrashed() : false);
                            newFile.setOwnerEmail(dto.getOwnerEmail());
                            newFile.setIndexedAt(now);
                            DriveFile saved = driveFileRepository.save(newFile);
                            syncPermissionsForSingleFile(account, saved, gFile.getPermissions());
                            newFiles++;
                        }
                    }
                }

                newStartPageToken = body.getNewStartPageToken();
                pageToken = body.getNextPageToken();
            } while (pageToken != null && !pageToken.trim().isEmpty());

            if (newStartPageToken != null) {
                account.setChangeToken(newStartPageToken);
            }
            account.setLastSyncedAt(now);
            googleAccountRepository.save(account);

            syncStorageQuota(account);
            storageSnapshotService.recordSnapshot(account);

            try {
                duplicateDetectionService.detectDuplicates(account.getId());
                oldFileAnalysisService.analyzeOldFiles(account.getId(), null);
                largeFileAnalysisService.analyzeLargeFiles(account.getId(), null);
                permissionAnalysisService.analyzeExternalShares(account.getId());
            } catch (Exception ex) {
                log.warn("Hygiene analysis post-incremental sync warning: {}", ex.getMessage());
            }

            scanRunService.completeScanRun(scanRun, changesProcessed, newFiles, updatedFiles, 0);

            log.info("Incremental sync completed for {}: changes={}, new={}, updated={}",
                    account.getEmail(), changesProcessed, newFiles, updatedFiles);

            return new ScanSummaryResponse(changesProcessed, newFiles, updatedFiles);
        } catch (Exception ex) {
            log.warn("Incremental sync failed or change token expired for {}: {}. Falling back to full scan.",
                    account.getEmail(), ex.getMessage());
            scanRunService.failScanRun(scanRun, "Token expired or error: " + ex.getMessage() + "; falling back to full scan.");
            return scanAndSyncFiles(googleAccountId);
        }
    }

    /**
     * Fetches current startPageToken from Google Drive API.
     */
    public String fetchStartPageToken(GoogleAccount account) {
        try {
            String accessToken = googleOAuthService.getValidAccessToken(account);
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(DRIVE_START_PAGE_TOKEN_URL)
                    .queryParam("supportsAllDrives", true);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            URI requestUri = uriBuilder.build().encode().toUri();
            ResponseEntity<GoogleDriveStartPageTokenResponse> response = restTemplate.exchange(
                    requestUri, HttpMethod.GET, request,
                    GoogleDriveStartPageTokenResponse.class
            );

            if (response.getBody() != null) {
                return response.getBody().getStartPageToken();
            }
        } catch (Exception ex) {
            log.warn("Could not fetch startPageToken for account {}: {}", account.getEmail(), ex.getMessage());
        }
        return null;
    }

    /**
     * Syncs live storage quota from Google Drive API (about?fields=user,storageQuota).
     */
    @Transactional
    public StorageQuotaResponse syncStorageQuota(GoogleAccount account) {
        try {
            String accessToken = googleOAuthService.getValidAccessToken(account);
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(DRIVE_ABOUT_API_URL)
                    .queryParam("fields", "user,storageQuota");

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            URI requestUri = uriBuilder.build().encode().toUri();
            ResponseEntity<GoogleDriveAboutResponse> response = restTemplate.exchange(
                    requestUri,
                    HttpMethod.GET,
                    request,
                    GoogleDriveAboutResponse.class
            );

            GoogleDriveAboutResponse body = response.getBody();
            if (body != null && body.getStorageQuota() != null) {
                GoogleDriveAboutResponse.StorageQuota quota = body.getStorageQuota();
                Long limit = parseLongSafe(quota.getLimit());
                Long usage = parseLongSafe(quota.getUsage());
                Long usageInDrive = parseLongSafe(quota.getUsageInDrive());
                Long usageInDriveTrash = parseLongSafe(quota.getUsageInDriveTrash());

                account.setStorageQuotaLimit(limit);
                account.setStorageQuotaUsage(usage);
                account.setStorageQuotaUsageInDrive(usageInDrive);
                account.setStorageQuotaUsageInDriveTrash(usageInDriveTrash);
                googleAccountRepository.save(account);

                log.info("Synced storage quota for account {}: usage={} bytes, limit={} bytes",
                        account.getEmail(), usage, limit);
                return new StorageQuotaResponse(
                        account.getId(), account.getEmail(), limit, usage, usageInDrive, usageInDriveTrash, LocalDateTime.now()
                );
            }
        } catch (Exception ex) {
            log.warn("Could not sync storage quota from Google Drive for account {}: {}", account.getEmail(), ex.getMessage());
        }

        return new StorageQuotaResponse(
                account.getId(),
                account.getEmail(),
                account.getStorageQuotaLimit(),
                account.getStorageQuotaUsage(),
                account.getStorageQuotaUsageInDrive(),
                account.getStorageQuotaUsageInDriveTrash(),
                account.getLastSyncedAt()
        );
    }

    /**
     * Retrieves storage quota for an account, fetching live if not yet stored.
     */
    public StorageQuotaResponse getStorageQuota(Long googleAccountId) {
        GoogleAccount account = getAccount(googleAccountId);
        if (account.getStorageQuotaLimit() == null || account.getStorageQuotaUsage() == null) {
            return syncStorageQuota(account);
        }
        return new StorageQuotaResponse(
                account.getId(),
                account.getEmail(),
                account.getStorageQuotaLimit(),
                account.getStorageQuotaUsage(),
                account.getStorageQuotaUsageInDrive(),
                account.getStorageQuotaUsageInDriveTrash(),
                account.getLastSyncedAt()
        );
    }

    /**
     * Syncs permissions for an individual file (used during incremental changes).
     */
    private void syncPermissionsForSingleFile(GoogleAccount account, DriveFile driveFile, List<GoogleDrivePermission> permissions) {
        if (permissions == null || permissions.isEmpty() || driveFile == null || driveFile.getId() == null) {
            return;
        }
        permissionRepository.deleteByDriveFileId(driveFile.getId());
        permissionRepository.flush();

        Set<String> seenPerms = new HashSet<>();
        for (GoogleDrivePermission gPerm : permissions) {
            if (gPerm.getId() == null || gPerm.getType() == null || gPerm.getRole() == null) {
                continue;
            }
            if (!seenPerms.add(gPerm.getId())) {
                continue;
            }
            Permission perm = new Permission();
            perm.setGoogleAccount(account);
            perm.setDriveFile(driveFile);
            perm.setPermissionId(gPerm.getId());
            perm.setType(gPerm.getType());
            perm.setRole(gPerm.getRole());
            perm.setEmailAddress(gPerm.getEmailAddress());
            perm.setDisplayName(gPerm.getDisplayName());
            perm.setDomain(gPerm.getDomain());
            perm.setAllowFileDiscovery(gPerm.getAllowFileDiscovery());
            perm.setExpirationTime(parseIsoDateTime(gPerm.getExpirationTime()));
            permissionRepository.save(perm);
        }
    }

    /**
     * Retrieves all indexed files from local MySQL for a specific Google Account.
     */
    public List<DriveFileResponse> getStoredFiles(Long googleAccountId) {
        GoogleAccount account = getAccount(googleAccountId);
        List<DriveFile> files = driveFileRepository.findByGoogleAccount_Id(account.getId());

        List<DriveFileResponse> responses = new ArrayList<>();
        for (DriveFile file : files) {
            responses.add(mapToDriveFileResponse(file));
        }
        return responses;
    }

    /**
     * Syncs permissions from raw Google Drive API items for a given account.
     * Flushes deletion and de-duplicates per file to avoid UK constraint violations.
     */
    public void syncPermissionsFromItems(GoogleAccount account,
                                         List<GoogleDriveFileItem> items,
                                         Map<String, DriveFile> savedFileMap) {
        permissionRepository.deleteByGoogleAccountId(account.getId());
        permissionRepository.flush();

        List<Permission> permissionsToSave = new ArrayList<>();
        Set<String> seenPerms = new HashSet<>();

        for (GoogleDriveFileItem item : items) {
            if (item.getPermissions() == null || item.getPermissions().isEmpty()) {
                continue;
            }
            DriveFile driveFile = savedFileMap.get(item.getId());
            if (driveFile == null || driveFile.getId() == null) {
                continue;
            }
            for (GoogleDrivePermission gPerm : item.getPermissions()) {
                if (gPerm.getId() == null || gPerm.getType() == null || gPerm.getRole() == null) {
                    continue;
                }
                String key = driveFile.getId() + ":" + gPerm.getId();
                if (!seenPerms.add(key)) {
                    continue;
                }
                Permission perm = new Permission();
                perm.setGoogleAccount(account);
                perm.setDriveFile(driveFile);
                perm.setPermissionId(gPerm.getId());
                perm.setType(gPerm.getType());
                perm.setRole(gPerm.getRole());
                perm.setEmailAddress(gPerm.getEmailAddress());
                perm.setDisplayName(gPerm.getDisplayName());
                perm.setDomain(gPerm.getDomain());
                perm.setAllowFileDiscovery(gPerm.getAllowFileDiscovery());
                perm.setExpirationTime(parseIsoDateTime(gPerm.getExpirationTime()));
                permissionsToSave.add(perm);
            }
        }

        if (!permissionsToSave.isEmpty()) {
            permissionRepository.saveAll(permissionsToSave);
            log.info("Synced {} permission records for account {}", permissionsToSave.size(), account.getEmail());
        }
    }

    /**
     * Resolves GoogleAccount by ID, or defaults to first available connected account.
     */
    public GoogleAccount getAccount(Long googleAccountId) {
        if (googleAccountId != null) {
            return googleAccountRepository.findById(googleAccountId)
                    .orElseGet(() -> {
                        List<GoogleAccount> accounts = googleAccountRepository.findAll();
                        if (!accounts.isEmpty()) {
                            return accounts.get(0);
                        }
                        throw new ResourceNotFoundException("Google account not found with id: " + googleAccountId);
                    });
        }

        List<GoogleAccount> accounts = googleAccountRepository.findAll();
        if (accounts.isEmpty()) {
            throw new ResourceNotFoundException("No connected Google account found. Please connect your Google Drive first.");
        }
        return accounts.get(0);
    }

    private DriveFileDto mapToDriveFileDto(GoogleDriveFileItem item) {
        String parentId = null;
        if (item.getParents() != null && !item.getParents().isEmpty()) {
            parentId = item.getParents().get(0);
        }

        String ownerEmail = null;
        if (item.getOwners() != null && !item.getOwners().isEmpty()) {
            ownerEmail = item.getOwners().get(0).getEmailAddress();
        }

        LocalDateTime createdTime = parseIsoDateTime(item.getCreatedTime());
        LocalDateTime modifiedTime = parseIsoDateTime(item.getModifiedTime());

        return new DriveFileDto(
                item.getId(),
                item.getName(),
                item.getMimeType(),
                item.getSize() != null ? item.getSize() : 0L,
                createdTime,
                modifiedTime,
                parentId,
                item.getWebViewLink(),
                item.getMd5Checksum(),
                item.getTrashed() != null ? item.getTrashed() : false,
                ownerEmail
        );
    }

    public DriveFileResponse mapToDriveFileResponse(DriveFile file) {
        return new DriveFileResponse(
                file.getId(),
                file.getGoogleFileId(),
                file.getGoogleAccountId(),
                file.getName(),
                file.getMimeType(),
                file.getSize(),
                file.getCreatedTime(),
                file.getModifiedTime(),
                file.getWebUrl(),
                file.getParentId(),
                file.getOwnerEmail(),
                file.getMd5Checksum(),
                file.getTrashed(),
                file.getIndexedAt()
        );
    }

    private LocalDateTime parseIsoDateTime(String isoString) {
        if (isoString == null || isoString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.ofInstant(Instant.parse(isoString), ZoneOffset.UTC);
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLongSafe(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private OAuthException translateGoogleDriveException(Exception ex, GoogleAccount account) {
        String responseBody = "";
        if (ex instanceof HttpClientErrorException httpException) {
            responseBody = httpException.getResponseBodyAsString();
        }

        String details = responseBody.isBlank() ? ex.getMessage() : responseBody;
        if (details != null && (details.contains("SERVICE_DISABLED") || details.contains("accessNotConfigured"))) {
            String message = "Google Drive API is disabled for the Google Cloud project used by this OAuth client. "
                    + "Enable Google Drive API for project 593111858859, wait a few minutes, then reconnect Google and scan again.";
            log.error("Google Drive API disabled for account {}: {}", account.getEmail(), ex.getMessage());
            return new OAuthException(message, ex);
        }

        log.error("Failed to fetch files from Google Drive for account {}: {}", account.getEmail(), ex.getMessage());
        return new OAuthException("Error communicating with Google Drive API: " + ex.getMessage(), ex);
    }
}
