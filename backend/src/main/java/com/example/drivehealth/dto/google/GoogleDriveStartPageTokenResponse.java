package com.example.drivehealth.dto.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleDriveStartPageTokenResponse {

    @JsonProperty("startPageToken")
    private String startPageToken;

    public GoogleDriveStartPageTokenResponse() {
    }

    public GoogleDriveStartPageTokenResponse(String startPageToken) {
        this.startPageToken = startPageToken;
    }

    public String getStartPageToken() {
        return startPageToken;
    }

    public void setStartPageToken(String startPageToken) {
        this.startPageToken = startPageToken;
    }
}
