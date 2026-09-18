package com.example.drivehealth.dto;

/**
 * Stage 12: Request body for updating a finding's status.
 */
public class UpdateFindingStatusRequest {

    private String status;

    public UpdateFindingStatusRequest() {
    }

    public UpdateFindingStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
