package com.example.drivehealth.controller;

import com.example.drivehealth.dto.AnalysisFindingResponse;
import com.example.drivehealth.dto.UpdateFindingStatusRequest;
import com.example.drivehealth.entity.FindingStatus;
import com.example.drivehealth.service.AnalysisFindingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing hygiene analysis findings and user decisions (Stage 12).
 */
@RestController
@RequestMapping("/api/findings")
public class FindingsController {

    private final AnalysisFindingService analysisFindingService;

    public FindingsController(AnalysisFindingService analysisFindingService) {
        this.analysisFindingService = analysisFindingService;
    }

    /**
     * Stage 12: Updates the status of an analysis finding.
     * 
     * Supported statuses:
     * - OPEN
     * - IGNORED
     * - RESOLVED
     * 
     * Important: This never deletes or alters actual Google Drive files.
     * 
     * Example request:
     * PATCH /api/findings/42
     * Body: { "status": "RESOLVED" }
     */
    @PatchMapping("/{id}")
    public ResponseEntity<AnalysisFindingResponse> updateStatus(
            @PathVariable("id") Long id,
            @RequestBody UpdateFindingStatusRequest request) {

        if (request == null || request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Status must be provided (e.g. OPEN, IGNORED, RESOLVED)");
        }

        FindingStatus status;
        try {
            status = FindingStatus.valueOf(request.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid status: " + request.getStatus() + ". Valid values are OPEN, IGNORED, RESOLVED.");
        }

        AnalysisFindingResponse response = analysisFindingService.updateFindingStatus(id, status);
        return ResponseEntity.ok(response);
    }
}
