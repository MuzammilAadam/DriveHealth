package com.example.drivehealth.service;

import com.example.drivehealth.dto.AuthSuccessResponse;
import com.example.drivehealth.dto.GoogleAccountResponse;
import com.example.drivehealth.dto.GoogleTokenResponse;
import com.example.drivehealth.dto.GoogleUserInfoResponse;
import com.example.drivehealth.dto.UserResponse;
import com.example.drivehealth.entity.GoogleAccount;
import com.example.drivehealth.entity.User;
import com.example.drivehealth.exception.OAuthException;
import com.example.drivehealth.exception.ResourceNotFoundException;
import com.example.drivehealth.repository.GoogleAccountRepository;
import com.example.drivehealth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for Google OAuth 2.0 flow:
 * 1. Building the Google OAuth Consent URL
 * 2. Exchanging the authorization code for Access & Refresh tokens
 * 3. Fetching user profile information from Google
 * 4. Storing/updating User and GoogleAccount entities
 * 5. Refreshing expired access tokens when needed
 */
@Service
public class GoogleOAuthService {

    private static final Logger log = LoggerFactory.getLogger(GoogleOAuthService.class);

    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    // Required Google Scopes:
    // - openid, email, profile: identity and basic profile information
    // - drive.metadata.readonly: read file and folder metadata (read-only, does not download file contents)
    private static final String SCOPES = "openid email profile https://www.googleapis.com/auth/drive.metadata.readonly";

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    private final UserRepository userRepository;
    private final GoogleAccountRepository googleAccountRepository;
    private final RestTemplate restTemplate;

    public GoogleOAuthService(UserRepository userRepository,
                              GoogleAccountRepository googleAccountRepository,
                              RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.googleAccountRepository = googleAccountRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Step 1 of OAuth 2.0:
     * Builds the Google Authorization URL where the user will be directed to give consent.
     */
    public String buildAuthorizationUrl() {
        return UriComponentsBuilder.fromHttpUrl(GOOGLE_AUTH_URL)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", SCOPES)
                // access_type=offline is required so Google returns a refresh_token
                .queryParam("access_type", "offline")
                // prompt=consent ensures Google shows the consent screen and provides a refresh_token every time
                .queryParam("prompt", "consent")
                .build()
                .toUriString();
    }

    /**
     * Step 2 & 3 of OAuth 2.0:
     * Handles the callback from Google containing the authorization code:
     * - Exchanges code for tokens
     * - Fetches user profile
     * - Persists User and GoogleAccount in the database
     */
    @Transactional
    public AuthSuccessResponse processOAuthCallback(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new OAuthException("Authorization code cannot be empty");
        }

        // 1. Exchange the authorization code for Google tokens
        GoogleTokenResponse tokenResponse = exchangeCodeForTokens(code);

        // 2. Use the access token to fetch user profile from Google
        GoogleUserInfoResponse userInfo = fetchGoogleUserInfo(tokenResponse.getAccessToken());

        // 3. Find or create the local application User (identified by email)
        User user = findOrCreateUser(userInfo.getEmail(), userInfo.getName());

        // 4. Find or create the connected GoogleAccount (identified by Google sub id)
        GoogleAccount googleAccount = findOrCreateGoogleAccount(user, userInfo, tokenResponse);

        log.info("Successfully connected Google Account: {} for User: {}", googleAccount.getEmail(), user.getEmail());

        // 5. Construct safe responses without exposing tokens
        UserResponse userResponse = mapToUserResponse(user);
        GoogleAccountResponse accountResponse = mapToGoogleAccountResponse(googleAccount);

        return new AuthSuccessResponse("Google account connected successfully", userResponse, accountResponse);
    }

    /**
     * Makes a POST request to Google's token endpoint to exchange the authorization code for tokens.
     */
    private GoogleTokenResponse exchangeCodeForTokens(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("code", code);
            requestBody.add("client_id", clientId);
            requestBody.add("client_secret", clientSecret);
            requestBody.add("redirect_uri", redirectUri);
            requestBody.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<GoogleTokenResponse> response = restTemplate.postForEntity(
                    GOOGLE_TOKEN_URL,
                    request,
                    GoogleTokenResponse.class
            );

            if (response.getBody() == null || response.getBody().getAccessToken() == null) {
                throw new OAuthException("Failed to obtain access token from Google");
            }

            return response.getBody();
        } catch (Exception ex) {
            log.error("Error exchanging code for tokens: {}", ex.getMessage());
            throw new OAuthException("Error communicating with Google OAuth token service: " + ex.getMessage(), ex);
        }
    }

    /**
     * Fetches user profile (email, name, picture) using the access token.
     */
    private GoogleUserInfoResponse fetchGoogleUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<GoogleUserInfoResponse> response = restTemplate.exchange(
                    GOOGLE_USERINFO_URL,
                    HttpMethod.GET,
                    request,
                    GoogleUserInfoResponse.class
            );

            if (response.getBody() == null || response.getBody().getEmail() == null) {
                throw new OAuthException("Failed to fetch user profile from Google");
            }

            return response.getBody();
        } catch (Exception ex) {
            log.error("Error fetching Google userinfo: {}", ex.getMessage());
            throw new OAuthException("Error fetching user profile from Google: " + ex.getMessage(), ex);
        }
    }

    /**
     * Refreshes an expired access token using the stored refresh token.
     */
    @Transactional
    public String refreshAccessToken(GoogleAccount account) {
        if (account.getRefreshToken() == null) {
            throw new OAuthException("No refresh token available for Google account: " + account.getEmail());
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("client_id", clientId);
            requestBody.add("client_secret", clientSecret);
            requestBody.add("refresh_token", account.getRefreshToken());
            requestBody.add("grant_type", "refresh_token");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<GoogleTokenResponse> response = restTemplate.postForEntity(
                    GOOGLE_TOKEN_URL,
                    request,
                    GoogleTokenResponse.class
            );

            GoogleTokenResponse body = response.getBody();
            if (body == null || body.getAccessToken() == null) {
                throw new OAuthException("Failed to refresh access token with Google");
            }

            // Update account tokens
            account.setAccessToken(body.getAccessToken());
            if (body.getExpiresIn() != null) {
                account.setTokenExpiresAt(LocalDateTime.now().plusSeconds(body.getExpiresIn()));
            }
            // Sometimes Google returns a new refresh token; only update if present
            if (body.getRefreshToken() != null) {
                account.setRefreshToken(body.getRefreshToken());
            }

            googleAccountRepository.save(account);
            return body.getAccessToken();
        } catch (Exception ex) {
            log.error("Error refreshing token for account {}: {}", account.getEmail(), ex.getMessage());
            throw new OAuthException("Could not refresh Google access token: " + ex.getMessage(), ex);
        }
    }

    /**
     * Retrieves a valid access token for the given GoogleAccount, automatically
     * refreshing it if expired.
     */
    public String getValidAccessToken(GoogleAccount account) {
        if (account.isAccessTokenExpired()) {
            log.info("Access token for {} has expired. Refreshing token...", account.getEmail());
            return refreshAccessToken(account);
        }
        return account.getAccessToken();
    }

    private User findOrCreateUser(String email, String name) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (name != null && !name.trim().isEmpty()) {
                user.setName(name);
            }
            return userRepository.save(user);
        }

        User newUser = new User(email, name != null ? name : email);
        return userRepository.save(newUser);
    }

    private GoogleAccount findOrCreateGoogleAccount(User user, GoogleUserInfoResponse userInfo, GoogleTokenResponse tokenResponse) {
        Optional<GoogleAccount> existingAccount = googleAccountRepository.findByGoogleUserId(userInfo.getSub());

        GoogleAccount account;
        if (existingAccount.isPresent()) {
            account = existingAccount.get();
            // Re-link to user if needed
            account.setUser(user);
        } else {
            account = new GoogleAccount();
            account.setUser(user);
            account.setGoogleUserId(userInfo.getSub());
        }

        account.setEmail(userInfo.getEmail());
        account.setName(userInfo.getName());
        account.setPictureUrl(userInfo.getPicture());
        account.setAccessToken(tokenResponse.getAccessToken());

        // Only overwrite refresh token if Google returned one (Google only returns it when consent is granted)
        if (tokenResponse.getRefreshToken() != null) {
            account.setRefreshToken(tokenResponse.getRefreshToken());
        }

        if (tokenResponse.getExpiresIn() != null) {
            account.setTokenExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn()));
        }

        account.setScope(tokenResponse.getScope());

        return googleAccountRepository.save(account);
    }

    /**
     * Helper to map User entity to UserResponse DTO.
     */
    public UserResponse mapToUserResponse(User user) {
        List<GoogleAccountResponse> accountResponses = new ArrayList<>();
        if (user.getGoogleAccounts() != null) {
            for (GoogleAccount account : user.getGoogleAccounts()) {
                accountResponses.add(mapToGoogleAccountResponse(account));
            }
        }
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getCreatedAt(),
                accountResponses
        );
    }

    /**
     * Helper to map GoogleAccount entity to GoogleAccountResponse DTO.
     * SECURITY: Never maps accessToken or refreshToken!
     */
    public GoogleAccountResponse mapToGoogleAccountResponse(GoogleAccount account) {
        return new GoogleAccountResponse(
                account.getId(),
                account.getGoogleUserId(),
                account.getEmail(),
                account.getName(),
                account.getPictureUrl(),
                account.getConnectedAt(),
                account.getLastSyncedAt()
        );
    }

    public UserResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return mapToUserResponse(user);
    }

    public UserResponse getCurrentUserProfile() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new ResourceNotFoundException("No user profile found. Please authenticate with Google first.");
        }
        return mapToUserResponse(users.get(0));
    }
}
