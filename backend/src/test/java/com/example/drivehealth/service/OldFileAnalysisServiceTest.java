package com.example.drivehealth.service;

import com.example.drivehealth.dto.OldFilesAnalysisResponse;
import com.example.drivehealth.entity.AnalysisFinding;
import com.example.drivehealth.entity.DriveFile;
import com.example.drivehealth.entity.FindingType;
import com.example.drivehealth.entity.GoogleAccount;
import com.example.drivehealth.entity.Severity;
import com.example.drivehealth.entity.User;
import com.example.drivehealth.repository.AnalysisFindingRepository;
import com.example.drivehealth.repository.DriveFileRepository;
import com.example.drivehealth.repository.GoogleAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OldFileAnalysisServiceTest {

    @Mock
    private GoogleAccountRepository googleAccountRepository;

    @Mock
    private DriveFileRepository driveFileRepository;

    @Mock
    private AnalysisFindingRepository analysisFindingRepository;

    @InjectMocks
    private OldFileAnalysisService oldFileAnalysisService;

    private GoogleAccount testAccount;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(oldFileAnalysisService, "defaultYearsThreshold", 2);

        User user = new User("developer@example.com", "Dev User");
        user.setId(1L);

        testAccount = new GoogleAccount();
        testAccount.setId(10L);
        testAccount.setUser(user);
        testAccount.setEmail("developer@gmail.com");
    }

    @Test
    void testAnalyzeOldFiles_FlagsFilesOlderThanThreshold() {
        when(googleAccountRepository.findById(10L)).thenReturn(Optional.of(testAccount));

        // File 1: Modified 3 years ago -> OLD
        DriveFile oldFile = new DriveFile();
        oldFile.setId(201L);
        oldFile.setName("Old_Thesis_2023.pdf");
        oldFile.setGoogleFileId("google-old-1");
        oldFile.setSize(4096L);
        oldFile.setModifiedTime(LocalDateTime.now().minusYears(3));
        oldFile.setGoogleAccount(testAccount);

        // File 2: Modified 1 month ago -> NOT old
        DriveFile recentFile = new DriveFile();
        recentFile.setId(202L);
        recentFile.setName("Recent_Notes.docx");
        recentFile.setGoogleFileId("google-recent-1");
        recentFile.setSize(2048L);
        recentFile.setModifiedTime(LocalDateTime.now().minusMonths(1));
        recentFile.setGoogleAccount(testAccount);

        // File 3: Modified 6 years ago -> VERY OLD (Severity HIGH)
        DriveFile veryOldFile = new DriveFile();
        veryOldFile.setId(203L);
        veryOldFile.setName("Archive_2020.zip");
        veryOldFile.setGoogleFileId("google-veryold-1");
        veryOldFile.setSize(10000L);
        veryOldFile.setModifiedTime(LocalDateTime.now().minusYears(6));
        veryOldFile.setGoogleAccount(testAccount);

        List<DriveFile> mockFiles = new ArrayList<>();
        mockFiles.add(oldFile);
        mockFiles.add(recentFile);
        mockFiles.add(veryOldFile);

        when(driveFileRepository.findByGoogleAccount_IdAndTrashedFalse(10L)).thenReturn(mockFiles);

        // Execute analysis with default 2 years threshold
        OldFilesAnalysisResponse response = oldFileAnalysisService.analyzeOldFiles(10L, null);

        // Verify previous findings cleared
        verify(analysisFindingRepository).deleteByGoogleAccountIdAndFindingType(10L, FindingType.OLD_FILE);

        // Verify findings saved
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AnalysisFinding>> captor = ArgumentCaptor.forClass(List.class);
        verify(analysisFindingRepository).saveAll(captor.capture());

        List<AnalysisFinding> savedFindings = captor.getValue();
        assertEquals(2, savedFindings.size(), "Should find 2 old files (> 2 years)");

        // Check response summary
        assertNotNull(response);
        assertEquals(2, response.getTotalOldFiles());
        assertEquals(14096L, response.getTotalOldStorageBytes(), "4096 + 10000 = 14096 bytes");
        assertEquals(2, response.getYearsThreshold());

        // Check finding details
        AnalysisFinding f1 = savedFindings.stream().filter(f -> f.getDriveFile().getId().equals(201L)).findFirst().orElseThrow();
        assertEquals(Severity.MEDIUM, f1.getSeverity());
        // Reason format: "Not modified for <N> days (exceeds <years> year threshold)"
        assertTrue(f1.getReason().startsWith("Not modified for "), "Reason should describe days since modification");
        assertTrue(f1.getReason().contains("2 year threshold"), "Reason should mention the configured threshold");

        AnalysisFinding f2 = savedFindings.stream().filter(f -> f.getDriveFile().getId().equals(203L)).findFirst().orElseThrow();
        assertEquals(Severity.HIGH, f2.getSeverity(), "Files older than 5 years should have HIGH severity");
    }
}
