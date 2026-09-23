package com.example.drivehealth.dto.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleDriveAboutResponse {

    private StorageQuota storageQuota;
    private User user;

    public StorageQuota getStorageQuota() {
        return storageQuota;
    }

    public void setStorageQuota(StorageQuota storageQuota) {
        this.storageQuota = storageQuota;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StorageQuota {
        private String limit;
        private String usage;
        private String usageInDrive;
        private String usageInDriveTrash;

        public String getLimit() {
            return limit;
        }

        public void setLimit(String limit) {
            this.limit = limit;
        }

        public String getUsage() {
            return usage;
        }

        public void setUsage(String usage) {
            this.usage = usage;
        }

        public String getUsageInDrive() {
            return usageInDrive;
        }

        public void setUsageInDrive(String usageInDrive) {
            this.usageInDrive = usageInDrive;
        }

        public String getUsageInDriveTrash() {
            return usageInDriveTrash;
        }

        public void setUsageInDriveTrash(String usageInDriveTrash) {
            this.usageInDriveTrash = usageInDriveTrash;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private String displayName;
        private String emailAddress;
        private String photoLink;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getEmailAddress() {
            return emailAddress;
        }

        public void setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
        }

        public String getPhotoLink() {
            return photoLink;
        }

        public void setPhotoLink(String photoLink) {
            this.photoLink = photoLink;
        }
    }
}
