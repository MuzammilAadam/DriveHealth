package com.example.drivehealth.controller;

import com.example.drivehealth.dto.ScanRunResponse;
import com.example.drivehealth.service.ScanRunService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for Stage 14 Scan History endpoints.
 */
@RestController
@RequestMapping("/api/scans")
public class ScanController {

    private final ScanRunService scanRunService;

    public ScanController(ScanRunService scanRunService) {
        this.scanRunService = scanRunService;
    }

    /**
     * Stage 14: Retrieves scan execution history.
     * 
     * Example requests:
     * - GET /api/scans
     * - GET /api/scans?accountId=1
     */
    @GetMapping
    public ResponseEntity<List<ScanRunResponse>> getScans(
            @RequestParam(name = "accountId", required = false) Long accountId) {
        List<ScanRunResponse> scans = scanRunService.getScans(accountId);
        return ResponseEntity.ok(scans);
    }
}
