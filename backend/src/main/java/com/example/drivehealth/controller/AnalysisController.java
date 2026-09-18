package com.example.drivehealth.controller;

import com.example.drivehealth.dto.AnalysisFindingResponse;
import com.example.drivehealth.dto.DuplicateResponse;
import com.example.drivehealth.dto.ExternalSharesAnalysisResponse;
import com.example.drivehealth.dto.LargeFileAnalysisResponse;
import com.example.drivehealth.dto.OldFilesAnalysisResponse;
import com.example.drivehealth.entity.FindingStatus;
import com.example.drivehealth.entity.FindingType;
import com.example.drivehealth.entity.Severity;
import com.example.drivehealth.service.AnalysisFindingService;
import com.example.drivehealth.service.DuplicateDetectionService;
import com.example.drivehealth.service.LargeFileAnalysisService;
import com.example.drivehealth.service.OldFileAnalysisService;
import com.example.drivehealth.service.PermissionAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for Drive hygiene analysis endpoints:
 * - Duplicate detection
 * - Old file analysis
 * - Universal findings querying
 */
@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final DuplicateDetectionService duplicateDetectionService;
    private final AnalysisFindingService analysisFindingService;
    private final OldFileAnalysisService oldFileAnalysisService;
    private final LargeFileAnalysisService largeFileAnalysisService;
    private final PermissionAnalysisService permissionAnalysisService;

    public AnalysisController(DuplicateDetectionService duplicateDetectionService,
                              AnalysisFindingService analysisFindingService,
                              OldFileAnalysisService oldFileAnalysisService,
                              LargeFileAnalysisService largeFileAnalysisService,
                              PermissionAnalysisService permissionAnalysisService) {
        this.duplicateDetectionService = duplicateDetectionService;
        this.analysisFindingService = analysisFindingService;
        this.oldFileAnalysisService = oldFileAnalysisService;
        this.largeFileAnalysisService = largeFileAnalysisService;
        this.permissionAnalysisService = permissionAnalysisService;
    }

    /**
     * Stage 5: Retrieves exact duplicate groups detected in Google Drive.
     * 
     * Example requests:
     * - GET /api/analysis/duplicates
     * - GET /api/analysis/duplicates?accountId=1
     * - GET /api/analysis/duplicates?refresh=true
     */
    @GetMapping("/duplicates")
    public ResponseEntity<DuplicateResponse> getDuplicates(
            @RequestParam(name = "accountId", required = false) Long accountId,
            @RequestParam(name = "refresh", defaultValue = "false") boolean refresh) {

        DuplicateResponse response = duplicateDetectionService.getDuplicates(accountId, refresh);
        return ResponseEntity.ok(response);
    }

    /**
     * Stage 5: Triggers a fresh duplicate detection scan.
     * 
     * Example request:
     * POST /api/analysis/duplicates/run
     * POST /api/analysis/duplicates/run?accountId=1
     */
    @PostMapping("/duplicates/run")
    public ResponseEntity<DuplicateResponse> runDuplicateDetection(
            @RequestParam(name = "accountId", required = false) Long accountId) {

        DuplicateResponse response = duplicateDetectionService.detectDuplicates(accountId);
        return ResponseEntity.ok(response);
    }

    /**
     * Stage 7: Analyzes and retrieves files that haven't been modified for a long time.
     * 
     * Example requests:
     * - GET /api/analysis/old-files
     * - GET /api/analysis/old-files?years=3
     * - GET /api/analysis/old-files?accountId=1&years=2
     * 
     * Example item in response:
     * {
     *   "totalOldFiles": 1,
     *   "totalOldStorageBytes": 1048576,
     *   "yearsThreshold": 2,
     *   "files": [
     *     {
     *       "name": "Project_2022.zip",
     *       "reason": "Not modified for more than 2 years",
     *       "daysSinceModified": 820
     *     }
     *   ]
     * }
     */
    @GetMapping("/old-files")
    public ResponseEntity<OldFilesAnalysisResponse> getOldFiles(
            @RequestParam(name = "accountId", required = false) Long accountId,
            @RequestParam(name = "years", required = false) Integer years) {

        OldFilesAnalysisResponse response = oldFileAnalysisService.analyzeOldFiles(accountId, years);
        return ResponseEntity.ok(response);
    }

    /**
     * Stage 7: Manually triggers an old file analysis scan.
     * 
     * Example request:
     * POST /api/analysis/old-files/run?years=2
     */
    @PostMapping("/old-files/run")
    public ResponseEntity<OldFilesAnalysisResponse> runOldFileAnalysis(
            @RequestParam(name = "accountId", required = false) Long accountId,
            @RequestParam(name = "years", required = false) Integer years) {

        OldFilesAnalysisResponse response = oldFileAnalysisService.analyzeOldFiles(accountId, years);
        return ResponseEntity.ok(response);
    }

    /**
     * Stage 6: Universal endpoint to retrieve analysis findings across all categories.
     * Supports filtering by accountId, findingType, severity, and status.
     * 
     * Example requests:
     * - GET /api/analysis/findings
     * - GET /api/analysis/findings?type=OLD_FILE
     * - GET /api/analysis/findings?severity=MEDIUM
     * - GET /api/analysis/findings?status=OPEN
     */
    @GetMapping("/findings")
    public ResponseEntity<List<AnalysisFindingResponse>> getFindings(
            @RequestParam(name = "accountId", required = false) Long accountId,
            @RequestParam(name = "type", required = false) FindingType type,
            @RequestParam(name = "severity", required = false) Severity severity,
            @RequestParam(name = "status", required = false) FindingStatus status) {

        List<AnalysisFindingResponse> response = analysisFindingService.getFindings(accountId, type, severity, status);
        return ResponseEntity.ok(response);
    }

    /**
     * Stage 8: Retrieves files exceeding the large-file size threshold.
     *
     * Example requests:
     * - GET /api/analysis/large-files
     * - GET /api/analysis/large-files?accountId=1
     * - GET /api/analysis/large-files?thresholdBytes=1073741824  (1 GB custom threshold)
     */
    @GetMapping("/large-files")
    public ResponseEntity<LargeFileAnalysisResponse> getLargeFiles(
            @RequestParam(name = "accountId", required = false) Long accountId,
            @RequestParam(name = "thresholdBytes", required = false) Long thresholdBytes) {

        LargeFileAnalysisResponse response = largeFileAnalysisService.analyzeLargeFiles(accountId, thresholdBytes);
        return ResponseEntity.ok(response);
    }

    /**
     * Stage 8: Triggers a fresh large file scan.
     *
     * Example request:
     * POST /api/analysis/large-files/run
     * POST /api/analysis/large-files/run?thresholdBytes=524288000
     */
    @PostMapping("/large-files/run")
    public ResponseEntity<LargeFileAnalysisResponse> runLargeFileAnalysis(
            @RequestParam(name = "accountId", required = false) Long accountId,
            @RequestParam(name = "thresholdBytes", required = false) Long thresholdBytes) {

        LargeFileAnalysisResponse response = largeFileAnalysisService.analyzeLargeFiles(accountId, thresholdBytes);
        return ResponseEntity.ok(response);
    }

    /**
     * Stage 9: Retrieves files with risky permission sharing (external users, public access).
     *
     * Example requests:
     * - GET /api/analysis/external-shares
     * - GET /api/analysis/external-shares?accountId=1
     */
    @GetMapping("/external-shares")
    public ResponseEntity<ExternalSharesAnalysisResponse> getExternalShares(
            @RequestParam(name = "accountId", required = false) Long accountId) {

        ExternalSharesAnalysisResponse response = permissionAnalysisService.analyzeExternalShares(accountId);
        return ResponseEntity.ok(response);
    }

    /**
     * Stage 9: Triggers a fresh external shares analysis scan.
     *
     * Example request:
     * POST /api/analysis/external-shares/run
     */
    @PostMapping("/external-shares/run")
    public ResponseEntity<ExternalSharesAnalysisResponse> runExternalSharesAnalysis(
            @RequestParam(name = "accountId", required = false) Long accountId) {

        ExternalSharesAnalysisResponse response = permissionAnalysisService.analyzeExternalShares(accountId);
        return ResponseEntity.ok(response);
    }
}

