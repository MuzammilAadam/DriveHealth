package com.example.drivehealth.service;

import com.example.drivehealth.dto.AnalysisFindingResponse;
import com.example.drivehealth.entity.AnalysisFinding;
import com.example.drivehealth.entity.DriveFile;
import com.example.drivehealth.entity.FindingStatus;
import com.example.drivehealth.entity.FindingType;
import com.example.drivehealth.entity.GoogleAccount;
import com.example.drivehealth.entity.Severity;
import com.example.drivehealth.entity.User;
import com.example.drivehealth.repository.AnalysisFindingRepository;
import com.example.drivehealth.repository.GoogleAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisFindingServiceTest {

    @Mock
    private GoogleAccountRepository googleAccountRepository;

    @Mock
    private AnalysisFindingRepository analysisFindingRepository;

    @InjectMocks
    private AnalysisFindingService analysisFindingService;

    private GoogleAccount testAccount;
    private DriveFile testFile;

    @BeforeEach
    void setUp() {
        User user = new User("developer@example.com", "Dev User");
        user.setId(1L);

        testAccount = new GoogleAccount();
        testAccount.setId(10L);
        testAccount.setUser(user);
        testAccount.setEmail("developer@gmail.com");

        testFile = new DriveFile();
        testFile.setId(100L);
        testFile.setName("OldDocument.pdf");
        testFile.setGoogleFileId("google-doc-123");
        testFile.setGoogleAccount(testAccount);
    }

    @Test
    void testGetFindings_ReturnsMappedResponses() {
        when(googleAccountRepository.findById(10L)).thenReturn(Optional.of(testAccount));

        AnalysisFinding finding = new AnalysisFinding(
                testAccount,
                testFile,
                FindingType.OLD_FILE,
                Severity.MEDIUM,
                100,
                "File has not been modified for more than 2 years"
        );
        finding.setId(1L);

        List<AnalysisFinding> mockList = new ArrayList<>();
        mockList.add(finding);

        when(analysisFindingRepository.findByGoogleAccountIdWithFile(10L)).thenReturn(mockList);

        List<AnalysisFindingResponse> results = analysisFindingService.getFindings(10L, null, null, null);

        assertNotNull(results);
        assertEquals(1, results.size());

        AnalysisFindingResponse resp = results.get(0);
        assertEquals(1L, resp.getId());
        assertEquals(100L, resp.getFileId());
        assertEquals("google-doc-123", resp.getGoogleFileId());
        assertEquals("OldDocument.pdf", resp.getFileName());
        assertEquals(FindingType.OLD_FILE, resp.getFindingType());
        assertEquals(Severity.MEDIUM, resp.getSeverity());
        assertEquals(100, resp.getConfidence());
        assertEquals("File has not been modified for more than 2 years", resp.getReason());
        assertEquals(FindingStatus.OPEN, resp.getStatus());
    }

    @Test
    void testUpdateFindingStatus_ResolvesFinding() {
        AnalysisFinding finding = new AnalysisFinding(
                testAccount,
                testFile,
                FindingType.DUPLICATE,
                Severity.LOW,
                100,
                "Duplicate file found"
        );
        finding.setId(5L);
        finding.setStatus(FindingStatus.OPEN);

        when(analysisFindingRepository.findById(5L)).thenReturn(Optional.of(finding));
        when(analysisFindingRepository.save(any(AnalysisFinding.class))).thenAnswer(i -> i.getArgument(0));

        AnalysisFindingResponse updated = analysisFindingService.updateFindingStatus(5L, FindingStatus.RESOLVED);

        assertNotNull(updated);
        assertEquals(FindingStatus.RESOLVED, updated.getStatus());
        assertNotNull(updated.getResolvedAt(), "resolvedAt should be timestamped upon resolving");
        verify(analysisFindingRepository).save(finding);
    }
}
