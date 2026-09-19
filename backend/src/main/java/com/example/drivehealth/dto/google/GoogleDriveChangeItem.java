package com.example.drivehealth.dto.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleDriveChangeItem {

    @JsonProperty("kind")
    private String kind;

    @JsonProperty("changeType")
    private String changeType;

    @JsonProperty("fileId")
    private String fileId;

    @JsonProperty("removed")
    private Boolean removed;

    @JsonProperty("time")
    private String time;

    @JsonProperty("file")
    private GoogleDriveFileItem file;

    public GoogleDriveChangeItem() {
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public Boolean getRemoved() {
        return removed;
    }

    public void setRemoved(Boolean removed) {
        this.removed = removed;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public GoogleDriveFileItem getFile() {
        return file;
    }

    public void setFile(GoogleDriveFileItem file) {
        this.file = file;
    }
}
