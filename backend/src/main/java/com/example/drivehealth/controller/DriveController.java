package com.example.drivehealth.controller;

import com.example.drivehealth.dto.DriveFileResponse;
import com.example.drivehealth.dto.DriveFilesPageResponse;
import com.example.drivehealth.dto.ScanSummaryResponse;
import com.example.drivehealth.service.GoogleDriveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Controller providing endpoints to interact with Google Drive API and manage local metadata.
 */
@RestController
@RequestMapping("/api/drive")
public class DriveController {

    private final GoogleDriveService googleDriveService;

    public DriveController(GoogleDriveService googleDriveService) {
        this.googleDriveService = googleDriveService;
    }

    /**
     * Stage 3: Retrieves raw files from Google Drive API with pagination.
     * 
     * Example requests:
     * - First page: GET /api/drive/files
     * - Next page:  GET /api/drive/files?pageToken=~!!~AI9FV7...
     * - Specific account: GET /api/drive/files?accountId=1&pageSize=25
     */
    @GetMapping("/files")
    public ResponseEntity<DriveFilesPageResponse> getFiles(
            @RequestParam(name = "accountId", required = false) Long accountId,
            @RequestParam(name = "pageSize", defaultValue = "50") int pageSize,
            @RequestParam(name = "pageToken", required = false) String pageToken) {

        DriveFilesPageResponse response = googleDriveService.getFilesPage(accountId, pageSize, pageToken);
        return ResponseEntity.ok(response);
    }

    /**
     * Stage 4: Scans Google Drive and stores/updates file metadata in MySQL.
     * Prevents duplicates by matching against googleFileId.
     * 
     * Example request:
     * POST /api/drive/scan
     * POST /api/drive/scan?accountId=1
     * 
     * Response:
     * {
     *   "filesScanned": 12481,
     *   "newFiles": 150,
     *   "updatedFiles": 37,
     *   "scannedAt": "2026-09-12T15:30:00"
     * }
     */
    @PostMapping("/scan")
    public ResponseEntity<ScanSummaryResponse> scanDrive(
            @RequestParam(name = "accountId", required = false) Long accountId,
            @RequestParam(name = "incremental", defaultValue = "false") boolean incremental) {

        ScanSummaryResponse response;
        if (incremental) {
            response = googleDriveService.syncChanges(accountId);
        } else {
            response = googleDriveService.scanAndSyncFiles(accountId);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Stage 4: Retrieves locally indexed files from MySQL.
     * 
     * Example request:
     * GET /api/drive/stored-files
     * GET /api/drive/stored-files?accountId=1
     */
    @GetMapping("/stored-files")
    public ResponseEntity<List<DriveFileResponse>> getStoredFiles(
            @RequestParam(name = "accountId", required = false) Long accountId) {

        List<DriveFileResponse> response = googleDriveService.getStoredFiles(accountId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves actual Google Drive storage quota (limit, usage, drive usage, trash).
     * Example: GET /api/drive/storage-quota?accountId=1
     */
    @GetMapping("/storage-quota")
    public ResponseEntity<com.example.drivehealth.dto.StorageQuotaResponse> getStorageQuota(
            @RequestParam(name = "accountId", required = false) Long accountId) {
        return ResponseEntity.ok(googleDriveService.getStorageQuota(accountId));
    }

    /**
     * Force refreshes actual Google Drive storage quota from Google Drive API.
     * Example: POST /api/drive/storage-quota/sync?accountId=1
     */
    @PostMapping("/storage-quota/sync")
    public ResponseEntity<com.example.drivehealth.dto.StorageQuotaResponse> syncStorageQuota(
            @RequestParam(name = "accountId", required = false) Long accountId) {
        var account = googleDriveService.getAccount(accountId);
        return ResponseEntity.ok(googleDriveService.syncStorageQuota(account));
    }

    /**
     * Deletes a file from Google Drive and removes it from the local DB.
     * DELETE /api/drive/files/{googleFileId}?accountId=1
     */
    @DeleteMapping("/files/{googleFileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable("googleFileId") String googleFileId,
            @RequestParam(name = "accountId", required = false) Long accountId) {
        googleDriveService.deleteFile(accountId, googleFileId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Uploads a file to Google Drive and indexes it locally.
     * POST /api/drive/files/upload?accountId=1&parentId=xxx
     */
    @PostMapping("/files/upload")
    public ResponseEntity<DriveFileResponse> uploadFile(
            @RequestParam(name = "accountId", required = false) Long accountId,
            @RequestParam(name = "parentId", required = false) String parentId,
            @RequestParam("file") MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload";
        String mimeType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        DriveFileResponse response = googleDriveService.uploadFile(accountId, originalFilename, mimeType, file.getBytes(), parentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new folder in Google Drive and indexes it locally.
     * POST /api/drive/folders?accountId=1&parentId=xxx&name=FolderName
     */
    @PostMapping("/folders")
    public ResponseEntity<DriveFileResponse> createFolder(
            @RequestParam(name = "accountId", required = false) Long accountId,
            @RequestParam(name = "parentId", required = false) String parentId,
            @RequestParam("name") String name) {
        DriveFileResponse response = googleDriveService.createFolder(accountId, name, parentId);
        return ResponseEntity.ok(response);
    }
}
