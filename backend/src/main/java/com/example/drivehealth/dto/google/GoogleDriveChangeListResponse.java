package com.example.drivehealth.dto.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleDriveChangeListResponse {

    @JsonProperty("kind")
    private String kind;

    @JsonProperty("nextPageToken")
    private String nextPageToken;

    @JsonProperty("newStartPageToken")
    private String newStartPageToken;

    @JsonProperty("changes")
    private List<GoogleDriveChangeItem> changes;

    public GoogleDriveChangeListResponse() {
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public String getNewStartPageToken() {
        return newStartPageToken;
    }

    public void setNewStartPageToken(String newStartPageToken) {
        this.newStartPageToken = newStartPageToken;
    }

    public List<GoogleDriveChangeItem> getChanges() {
        return changes;
    }

    public void setChanges(List<GoogleDriveChangeItem> changes) {
        this.changes = changes;
    }
}
