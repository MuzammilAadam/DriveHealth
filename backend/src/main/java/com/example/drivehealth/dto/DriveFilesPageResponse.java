package com.example.drivehealth.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Paginated response for Drive files.
 * Contains the list of files in the current page and the nextPageToken if more files exist.
 */
public class DriveFilesPageResponse {

    private List<DriveFileDto> files = new ArrayList<>();
    private String nextPageToken;
    private int count;

    public DriveFilesPageResponse() {
    }

    public DriveFilesPageResponse(List<DriveFileDto> files, String nextPageToken) {
        this.files = files;
        this.nextPageToken = nextPageToken;
        this.count = files != null ? files.size() : 0;
    }

    public List<DriveFileDto> getFiles() {
        return files;
    }

    public void setFiles(List<DriveFileDto> files) {
        this.files = files;
        this.count = files != null ? files.size() : 0;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
