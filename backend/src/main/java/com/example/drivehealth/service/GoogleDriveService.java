package com.example.drivehealth.service;

import com.example.drivehealth.dto.DriveFileDto;
import com.example.drivehealth.dto.DriveFileResponse;
import com.example.drivehealth.dto.DriveFilesPageResponse;
import com.example.drivehealth.dto.ScanSummaryResponse;
import com.example.drivehealth.dto.google.GoogleDriveFileItem;
import com.example.drivehealth.dto.google.GoogleDriveFileListResponse;
import com.example.drivehealth.dto.google.GoogleDrivePermission;
import com.example.drivehealth.entity.DriveFile;
import com.example.drivehealth.entity.GoogleAccount;
import com.example.drivehealth.entity.Permission;
import com.example.drivehealth.exception.OAuthException;
import com.example.drivehealth.exception.ResourceNotFoundException;
import com.example.drivehealth.repository.DriveFileRepository;
import com.example.drivehealth.repository.GoogleAccountRepository;
import com.example.drivehealth.repository.PermissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // Request file metadata fields + permissions in one call per page (no per-file extra requests)
    private static final String FIELDS_QUERY = "nextPageToken, files(id, name, mimeType, size, createdTime, modifiedTime, parents, webViewLink, md5Checksum, trashed, owners, permissions)";

    private final GoogleAccountRepository googleAccountRepository;
    private final DriveFileRepository driveFileRepository;
    private final GoogleOAuthService googleOAuthService;
    private final RestTemplate restTemplate;
    private final PermissionRepository permissionRepository;

    public GoogleDriveService(GoogleAccountRepository googleAccountRepository,
                              DriveFileRepository driveFileRepository,
                              GoogleOAuthService googleOAuthService,
                              RestTemplate restTemplate,
                              PermissionRepository permissionRepository) {
        this.googleAccountRepository = googleAccountRepository;
        this.driveFileRepository = driveFileRepository;
        this.googleOAuthService = googleOAuthService;
        this.restTemplate = restTemplate;
        this.permissionRepository = permissionRepository;
    }

    /**
     * Retrieves a single page of files directly from Google Drive API.
     * 
     * How Pagination Works:
     * Google Drive returns files in batches (pages).
     * If more files remain, Google includes a 'nextPageToken'.
     * Passing that token retrieves the next page until 'nextPageToken' is null.
     */
    public DriveFilesPageResponse getFilesPage(Long googleAccountId, int pageSize, String pageToken) {
        GoogleAccount account = getAccount(googleAccountId);

        // Ensure we have a fresh, valid access token (auto-refreshes if expired)
        String accessToken = googleOAuthService.getValidAccessToken(account);

        // Build the request URL with query parameters
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
            ResponseEntity<GoogleDriveFileListResponse> response = restTemplate.exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.GET,
                    request,
                    GoogleDriveFileListResponse.class
            );

            GoogleDriveFileListResponse body = response.getBody();
            if (body == null || body.getFiles() == null) {
                return new DriveFilesPageResponse(new ArrayList<>(), null);
            }

            // Convert each Google file item to our clean DTO
            List<DriveFileDto> dtoList = new ArrayList<>();
            for (GoogleDriveFileItem item : body.getFiles()) {
                dtoList.add(mapToDriveFileDto(item));
            }

            return new DriveFilesPageResponse(dtoList, body.getNextPageToken());
        } catch (Exception ex) {
            log.error("Failed to fetch files from Google Drive for account {}: {}", account.getEmail(), ex.getMessage());
            throw new OAuthException("Error communicating with Google Drive API: " + ex.getMessage(), ex);
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

            ResponseEntity<GoogleDriveFileListResponse> response = restTemplate.exchange(
                    uriBuilder.toUriString(), HttpMethod.GET, request, GoogleDriveFileListResponse.class);

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
     * Retrieves ALL files from Google Drive (as DTOs — for backward compat).
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
     *
     * Flow:
     * 1. Fetches all file metadata (+ permissions) from Google Drive API in batches.
     * 2. Pre-loads existing local DriveFile records into memory.
     * 3. Upserts DriveFile records using googleFileId.
     * 4. Syncs Permission records for all files.
     * 5. Updates GoogleAccount's lastSyncedAt.
     * 6. Returns a ScanSummaryResponse.
     */
    @Transactional
    public ScanSummaryResponse scanAndSyncFiles(Long googleAccountId) {
        GoogleAccount account = getAccount(googleAccountId);
        log.info("Starting scan and sync for Google Account: {}", account.getEmail());

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

        // Step 5: Sync permissions using raw items (they already carry permission data)
        List<DriveFile> allSavedFiles = driveFileRepository.findByGoogleAccount_Id(account.getId());
        Map<String, DriveFile> savedFileMap = new HashMap<>();
        for (DriveFile f : allSavedFiles) {
            savedFileMap.put(f.getGoogleFileId(), f);
        }
        syncPermissionsFromItems(account, rawItems, savedFileMap);

        // Step 6: Update account's lastSyncedAt
        account.setLastSyncedAt(now);
        googleAccountRepository.save(account);

        log.info("Scan completed for {}: scanned={}, new={}, updated={}",
                account.getEmail(), filesScanned, newFiles, updatedFiles);

        return new ScanSummaryResponse(filesScanned, newFiles, updatedFiles);
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
     * Called after a scan completes. Clears existing permissions for the account and re-inserts.
     */
    public void syncPermissionsFromItems(GoogleAccount account,
                                         List<GoogleDriveFileItem> items,
                                         Map<String, DriveFile> savedFileMap) {
        // Clear all existing permissions for this account before re-syncing
        permissionRepository.deleteByGoogleAccountId(account.getId());

        List<Permission> permissionsToSave = new ArrayList<>();

        for (GoogleDriveFileItem item : items) {
            if (item.getPermissions() == null || item.getPermissions().isEmpty()) {
                continue;
            }
            DriveFile driveFile = savedFileMap.get(item.getId());
            if (driveFile == null) {
                continue;
            }
            for (GoogleDrivePermission gPerm : item.getPermissions()) {
                if (gPerm.getId() == null || gPerm.getType() == null || gPerm.getRole() == null) {
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
     * Resolves GoogleAccount by ID, or defaults to the first connected account if ID is null.
     */
    public GoogleAccount getAccount(Long googleAccountId) {
        if (googleAccountId != null) {
            return googleAccountRepository.findById(googleAccountId)
                    .orElseThrow(() -> new ResourceNotFoundException("Google account not found with id: " + googleAccountId));
        }

        // Fallback: If no ID specified, take the first available account
        List<GoogleAccount> accounts = googleAccountRepository.findAll();
        if (accounts.isEmpty()) {
            throw new ResourceNotFoundException("No connected Google account found. Please connect your Google Drive first.");
        }
        return accounts.get(0);
    }

    /**
     * Converts a GoogleDriveFileItem from the Google API to our internal DriveFileDto.
     */
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

    /**
     * Converts a stored DriveFile entity to a DriveFileResponse DTO.
     */
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

    /**
     * Helper to safely parse ISO-8601 strings returned by Google (e.g. 2026-03-01T12:00:00.000Z)
     */
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
}
