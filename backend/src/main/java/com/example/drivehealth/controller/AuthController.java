package com.example.drivehealth.controller;

import com.example.drivehealth.dto.AuthSuccessResponse;
import com.example.drivehealth.dto.AuthUrlResponse;
import com.example.drivehealth.dto.UserResponse;
import com.example.drivehealth.service.GoogleOAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling Google OAuth 2.0 authentication endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final GoogleOAuthService googleOAuthService;

    public AuthController(GoogleOAuthService googleOAuthService) {
        this.googleOAuthService = googleOAuthService;
    }

    /**
     * Returns the Google OAuth 2.0 authorization URL.
     * The frontend redirects the user to this URL to initiate Google login & permission grant.
     */
    @GetMapping("/google/url")
    public ResponseEntity<AuthUrlResponse> getGoogleAuthUrl() {
        String authUrl = googleOAuthService.buildAuthorizationUrl();
        return ResponseEntity.ok(new AuthUrlResponse(authUrl));
    }

    /**
     * OAuth Callback endpoint.
     * Google redirects back here with the authorization code:
     * GET /api/auth/google/callback?code=4/0Af...
     */
    @GetMapping("/google/callback")
    public ResponseEntity<AuthSuccessResponse> handleGoogleCallback(@RequestParam("code") String code) {
        AuthSuccessResponse response = googleOAuthService.processOAuthCallback(code);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves user profile details and connected Google accounts for a specific user ID.
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable("userId") Long userId) {
        UserResponse user = googleOAuthService.getUserProfile(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Retrieves the current authenticated user's profile and connected Google accounts.
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe() {
        UserResponse user = googleOAuthService.getCurrentUserProfile();
        return ResponseEntity.ok(user);
    }
}
