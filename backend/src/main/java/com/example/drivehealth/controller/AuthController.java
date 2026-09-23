package com.example.drivehealth.controller;

import com.example.drivehealth.dto.AuthSuccessResponse;
import com.example.drivehealth.dto.AuthUrlResponse;
import com.example.drivehealth.dto.UserResponse;
import com.example.drivehealth.service.GoogleOAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Controller handling Google OAuth 2.0 authentication endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final GoogleOAuthService googleOAuthService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

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
     * If request comes from a browser, redirects back to the frontend SPA.
     */
    @GetMapping("/google/callback")
    public ResponseEntity<?> handleGoogleCallback(
            @RequestParam("code") String code,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        try {
            AuthSuccessResponse authSuccess = googleOAuthService.processOAuthCallback(code);

            String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);
            boolean isBrowserRequest = acceptHeader != null && acceptHeader.contains("text/html");

            if (isBrowserRequest) {
                UserResponse user = authSuccess.getUser();
                String redirectUri = String.format("%s/oauth/callback?userId=%d&email=%s&name=%s",
                        frontendUrl,
                        user.getId(),
                        URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8),
                        URLEncoder.encode(user.getName() != null ? user.getName() : "", StandardCharsets.UTF_8));
                return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUri)).build();
            }

            return ResponseEntity.ok(authSuccess);
        } catch (Exception e) {
            String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);
            boolean isBrowserRequest = acceptHeader != null && acceptHeader.contains("text/html");
            if (isBrowserRequest) {
                String errorRedirect = String.format("%s/login?error=%s",
                        frontendUrl,
                        URLEncoder.encode(e.getMessage() != null ? e.getMessage() : "OAuth failed", StandardCharsets.UTF_8));
                return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(errorRedirect)).build();
            }
            throw e;
        }
    }

    /**
     * Retrieves all registered users.
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = googleOAuthService.getAllUsers();
        return ResponseEntity.ok(users);
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
     * Optionally accepts X-User-Id header or userId query parameter.
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(
            @RequestParam(name = "userId", required = false) Long userIdParam,
            @RequestHeader(name = "X-User-Id", required = false) Long userIdHeader) {

        Long effectiveUserId = userIdHeader != null ? userIdHeader : userIdParam;
        UserResponse user = googleOAuthService.getCurrentUserProfile(effectiveUserId);
        return ResponseEntity.ok(user);
    }
}
