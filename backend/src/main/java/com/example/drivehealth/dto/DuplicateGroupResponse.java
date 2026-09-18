package com.example.drivehealth.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a detected duplicate group in API responses.
 * Matches Stage 5 format:
 * {
 *   "id": 1,
 *   "confidence": 100,
 *   "reason": "Files have the same checksum",
 *   "files": [ { "name": "resume.pdf" }, { "name": "resume_final.pdf" } ]
 * }
 */
public class DuplicateGroupResponse {

    private Long id;
    private int confidence;
    private String reason;
    private String md5Checksum;
    private Long individualFileSize;
    private Long wastedSize;
    private List<DuplicateFileItemResponse> files = new ArrayList<>();

    public DuplicateGroupResponse() {
    }

    public DuplicateGroupResponse(Long id, int confidence, String reason, String md5Checksum,
                                  Long individualFileSize, Long wastedSize,
                                  List<DuplicateFileItemResponse> files) {
        this.id = id;
        this.confidence = confidence;
        this.reason = reason;
        this.md5Checksum = md5Checksum;
        this.individualFileSize = individualFileSize;
        this.wastedSize = wastedSize;
        this.files = files;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getMd5Checksum() {
        return md5Checksum;
    }

    public void setMd5Checksum(String md5Checksum) {
        this.md5Checksum = md5Checksum;
    }

    public Long getIndividualFileSize() {
        return individualFileSize;
    }

    public void setIndividualFileSize(Long individualFileSize) {
        this.individualFileSize = individualFileSize;
    }

    public Long getWastedSize() {
        return wastedSize;
    }

    public void setWastedSize(Long wastedSize) {
        this.wastedSize = wastedSize;
    }

    public List<DuplicateFileItemResponse> getFiles() {
        return files;
    }

    public void setFiles(List<DuplicateFileItemResponse> files) {
        this.files = files;
    }
}
