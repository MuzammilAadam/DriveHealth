package com.example.drivehealth.controller;

import com.example.drivehealth.dto.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> checkHealth() {
        HealthResponse response = new HealthResponse("Drive Health backend is running");
        return ResponseEntity.ok(response);
    }
}
