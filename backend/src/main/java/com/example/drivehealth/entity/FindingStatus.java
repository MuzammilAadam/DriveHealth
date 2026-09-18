package com.example.drivehealth.entity;

/**
 * Stage 6: The current status of an analysis finding.
 * Findings start as OPEN, and can be marked as IGNORED or RESOLVED by the user.
 */
public enum FindingStatus {
    OPEN,
    IGNORED,
    RESOLVED
}
