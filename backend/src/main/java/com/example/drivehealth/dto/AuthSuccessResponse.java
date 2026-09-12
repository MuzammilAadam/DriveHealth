package com.example.drivehealth.dto;

/**
 * Returned after completing Google OAuth callback.
 */
public class AuthSuccessResponse {

    private String message;
    private UserResponse user;
    private GoogleAccountResponse googleAccount;

    public AuthSuccessResponse() {
    }

    public AuthSuccessResponse(String message, UserResponse user, GoogleAccountResponse googleAccount) {
        this.message = message;
        this.user = user;
        this.googleAccount = googleAccount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    public GoogleAccountResponse getGoogleAccount() {
        return googleAccount;
    }

    public void setGoogleAccount(GoogleAccountResponse googleAccount) {
        this.googleAccount = googleAccount;
    }
}
