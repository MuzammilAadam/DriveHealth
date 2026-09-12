package com.example.drivehealth.dto.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleDriveFileItem {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("mimeType")
    private String mimeType;

    @JsonProperty("size")
    private Long size;

    @JsonProperty("createdTime")
    private String createdTime;

    @JsonProperty("modifiedTime")
    private String modifiedTime;

    @JsonProperty("parents")
    private List<String> parents;

    @JsonProperty("webViewLink")
    private String webViewLink;

    @JsonProperty("md5Checksum")
    private String md5Checksum;

    @JsonProperty("trashed")
    private Boolean trashed;

    @JsonProperty("owners")
    private List<GoogleDriveOwner> owners;

    public GoogleDriveFileItem() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(String modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public List<String> getParents() {
        return parents;
    }

    public void setParents(List<String> parents) {
        this.parents = parents;
    }

    public String getWebViewLink() {
        return webViewLink;
    }

    public void setWebViewLink(String webViewLink) {
        this.webViewLink = webViewLink;
    }

    public String getMd5Checksum() {
        return md5Checksum;
    }

    public void setMd5Checksum(String md5Checksum) {
        this.md5Checksum = md5Checksum;
    }

    public Boolean getTrashed() {
        return trashed;
    }

    public void setTrashed(Boolean trashed) {
        this.trashed = trashed;
    }

    public List<GoogleDriveOwner> getOwners() {
        return owners;
    }

    public void setOwners(List<GoogleDriveOwner> owners) {
        this.owners = owners;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GoogleDriveOwner {
        @JsonProperty("emailAddress")
        private String emailAddress;

        @JsonProperty("displayName")
        private String displayName;

        public GoogleDriveOwner() {
        }

        public String getEmailAddress() {
            return emailAddress;
        }

        public void setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
    }
}
