package com.example.drivehealth.dto;

public class HealthResponse {

    private String message;

    public HealthResponse() {
    }

    public HealthResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
