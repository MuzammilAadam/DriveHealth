package com.example.drivehealth.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-level response for GET /api/analysis/duplicates.
 * Contains the list of groups plus summary statistics.
 */
public class DuplicateResponse {

    private List<DuplicateGroupResponse> groups = new ArrayList<>();
    private int totalDuplicateGroups;
    private int totalDuplicateFiles;
    private long totalWastedStorageBytes;

    public DuplicateResponse() {
    }

    public DuplicateResponse(List<DuplicateGroupResponse> groups, int totalDuplicateGroups,
                             int totalDuplicateFiles, long totalWastedStorageBytes) {
        this.groups = groups;
        this.totalDuplicateGroups = totalDuplicateGroups;
        this.totalDuplicateFiles = totalDuplicateFiles;
        this.totalWastedStorageBytes = totalWastedStorageBytes;
    }

    // Getters and Setters

    public List<DuplicateGroupResponse> getGroups() {
        return groups;
    }

    public void setGroups(List<DuplicateGroupResponse> groups) {
        this.groups = groups;
    }

    public int getTotalDuplicateGroups() {
        return totalDuplicateGroups;
    }

    public void setTotalDuplicateGroups(int totalDuplicateGroups) {
        this.totalDuplicateGroups = totalDuplicateGroups;
    }

    public int getTotalDuplicateFiles() {
        return totalDuplicateFiles;
    }

    public void setTotalDuplicateFiles(int totalDuplicateFiles) {
        this.totalDuplicateFiles = totalDuplicateFiles;
    }

    public long getTotalWastedStorageBytes() {
        return totalWastedStorageBytes;
    }

    public void setTotalWastedStorageBytes(long totalWastedStorageBytes) {
        this.totalWastedStorageBytes = totalWastedStorageBytes;
    }
}
