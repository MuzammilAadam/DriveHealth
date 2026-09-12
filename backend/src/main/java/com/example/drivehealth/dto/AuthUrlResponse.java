package com.example.drivehealth.dto;

public class AuthUrlResponse {

    private String authorizationUrl;

    public AuthUrlResponse() {
    }

    public AuthUrlResponse(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    public void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }
}
