package com.example.drivehealth.dto;

import java.util.List;

/**
 * Stage 9: Summary response for the external sharing / permissions analysis.
 */
public class ExternalSharesAnalysisResponse {

    private List<ExternalShareItemResponse> files;
    private int totalExternalShares;
    private int totalPublicFiles;
    private int totalFilesWithRiskyPermissions;

    public ExternalSharesAnalysisResponse(List<ExternalShareItemResponse> files,
                                           int totalExternalShares,
                                           int totalPublicFiles,
                                           int totalFilesWithRiskyPermissions) {
        this.files = files;
        this.totalExternalShares = totalExternalShares;
        this.totalPublicFiles = totalPublicFiles;
        this.totalFilesWithRiskyPermissions = totalFilesWithRiskyPermissions;
    }

    public List<ExternalShareItemResponse> getFiles() { return files; }
    public int getTotalExternalShares() { return totalExternalShares; }
    public int getTotalPublicFiles() { return totalPublicFiles; }
    public int getTotalFilesWithRiskyPermissions() { return totalFilesWithRiskyPermissions; }
}
