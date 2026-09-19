package com.example.drivehealth.controller;

import com.example.drivehealth.dto.UserRuleRequest;
import com.example.drivehealth.dto.UserRuleResponse;
import com.example.drivehealth.service.UserRuleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for managing user-configurable hygiene rules (Stage 13).
 */
@RestController
@RequestMapping("/api/rules")
public class UserRuleController {

    private final UserRuleService userRuleService;

    public UserRuleController(UserRuleService userRuleService) {
        this.userRuleService = userRuleService;
    }

    /**
     * Stage 13: Retrieves all configured hygiene rules.
     * 
     * Example request:
     * GET /api/rules
     * GET /api/rules?accountId=1
     */
    @GetMapping
    public ResponseEntity<List<UserRuleResponse>> getRules(
            @RequestParam(name = "accountId", required = false) Long accountId) {
        List<UserRuleResponse> rules = userRuleService.getRules(accountId);
        return ResponseEntity.ok(rules);
    }

    /**
     * Stage 13: Creates a new user hygiene rule.
     * 
     * Example requests:
     * - Ignore folder:
     *   POST /api/rules
     *   { "ruleType": "IGNORE_FOLDER", "ruleValue": "1B2c3D4e...", "description": "Ignore archive folder" }
     * - Ignore MIME type:
     *   POST /api/rules
     *   { "ruleType": "IGNORE_MIME_TYPE", "ruleValue": "video/mp4", "description": "Skip raw video clips" }
     * - Custom large file threshold:
     *   POST /api/rules
     *   { "ruleType": "LARGE_FILE_THRESHOLD", "ruleValue": "1073741824", "description": "1 GB large file cutoff" }
     */
    @PostMapping
    public ResponseEntity<UserRuleResponse> createRule(
            @RequestParam(name = "accountId", required = false) Long accountId,
            @RequestBody UserRuleRequest request) {
        UserRuleResponse created = userRuleService.createRule(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Stage 13: Deletes a user rule.
     * 
     * Example request:
     * DELETE /api/rules/5
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable("id") Long id) {
        userRuleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}
