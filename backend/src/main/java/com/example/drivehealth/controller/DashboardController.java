package com.example.drivehealth.controller;

import com.example.drivehealth.dto.DashboardSummaryResponse;
import com.example.drivehealth.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for Stage 10 Dashboard endpoints.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Stage 10: Aggregated dashboard summary for an account.
     * 
     * Example requests:
     * - GET /api/dashboard/summary
     * - GET /api/dashboard/summary?accountId=1
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @RequestParam(name = "accountId", required = false) Long accountId) {
        DashboardSummaryResponse response = dashboardService.getDashboardSummary(accountId);
        return ResponseEntity.ok(response);
    }
}
