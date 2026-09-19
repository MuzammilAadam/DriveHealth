package com.example.drivehealth.controller;

import com.example.drivehealth.dto.DriveFileResponse;
import com.example.drivehealth.dto.DriveFilesPageResponse;
import com.example.drivehealth.dto.ScanSummaryResponse;
import com.example.drivehealth.service.GoogleDriveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
