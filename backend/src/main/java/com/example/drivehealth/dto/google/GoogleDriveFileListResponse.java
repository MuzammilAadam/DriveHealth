package com.example.drivehealth.dto.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleDriveFileListResponse {

    @JsonProperty("nextPageToken")
    private String nextPageToken;

    @JsonProperty("files")
    private List<GoogleDriveFileItem> files;

    public GoogleDriveFileListResponse() {
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public List<GoogleDriveFileItem> getFiles() {
        return files;
    }

    public void setFiles(List<GoogleDriveFileItem> files) {
        this.files = files;
    }
}
