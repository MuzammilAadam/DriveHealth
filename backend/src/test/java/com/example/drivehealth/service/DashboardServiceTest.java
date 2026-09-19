package com.example.drivehealth.service;

import com.example.drivehealth.dto.DashboardSummaryResponse;
import com.example.drivehealth.entity.FindingStatus;
import com.example.drivehealth.entity.FindingType;
import com.example.drivehealth.entity.GoogleAccount;
import com.example.drivehealth.entity.Severity;
import com.example.drivehealth.entity.User;
import com.example.drivehealth.repository.AnalysisFindingRepository;
import com.example.drivehealth.repository.DriveFileRepository;
import com.example.drivehealth.repository.DuplicateGroupFileRepository;
import com.example.drivehealth.repository.DuplicateGroupRepository;
import com.example.drivehealth.repository.GoogleAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private GoogleAccountRepository googleAccountRepository;

    @Mock
    private DriveFileRepository driveFileRepository;

    @Mock
    private AnalysisFindingRepository analysisFindingRepository;

    @Mock
    private DuplicateGroupRepository duplicateGroupRepository;

    @Mock
    private DuplicateGroupFileRepository duplicateGroupFileRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private GoogleAccount testAccount;

    @BeforeEach
    void setUp() {
        User user = new User("dev@example.com", "Dev");
        user.setId(1L);

        testAccount = new GoogleAccount();
        testAccount.setId(10L);
        testAccount.setUser(user);
        testAccount.setEmail("dev@example.com");
    }

    @Test
    void testGetDashboardSummary() {
        when(googleAccountRepository.findById(10L)).thenReturn(Optional.of(testAccount));
        when(driveFileRepository.countByGoogleAccountId(10L)).thenReturn(100L);
        when(driveFileRepository.sumSizeByGoogleAccountId(10L)).thenReturn(104857600L); // 100 MB
        when(driveFileRepository.countByGoogleAccountIdAndTrashedTrue(10L)).thenReturn(5L);

        when(analysisFindingRepository.countByGoogleAccount_Id(10L)).thenReturn(12L);
        when(analysisFindingRepository.countByGoogleAccount_IdAndStatus(10L, FindingStatus.OPEN)).thenReturn(8L);
        when(analysisFindingRepository.countByGoogleAccount_IdAndStatus(10L, FindingStatus.IGNORED)).thenReturn(2L);
        when(analysisFindingRepository.countByGoogleAccount_IdAndStatus(10L, FindingStatus.RESOLVED)).thenReturn(2L);
        when(analysisFindingRepository.countByGoogleAccount_IdAndStatusAndSeverity(10L, FindingStatus.OPEN, Severity.HIGH)).thenReturn(3L);

        when(duplicateGroupRepository.countByGoogleAccount_Id(10L)).thenReturn(2L);
        when(duplicateGroupFileRepository.countByDuplicateGroup_GoogleAccount_Id(10L)).thenReturn(4L);
        when(analysisFindingRepository.countByGoogleAccount_IdAndFindingType(10L, FindingType.OLD_FILE)).thenReturn(4L);
        when(analysisFindingRepository.countByGoogleAccount_IdAndFindingType(10L, FindingType.LARGE_FILE)).thenReturn(2L);
        when(analysisFindingRepository.countByGoogleAccount_IdAndFindingType(10L, FindingType.EXTERNAL_SHARE)).thenReturn(1L);
        when(analysisFindingRepository.countByGoogleAccount_IdAndFindingType(10L, FindingType.PUBLIC_FILE)).thenReturn(1L);

        DashboardSummaryResponse summary = dashboardService.getDashboardSummary(10L);

        assertNotNull(summary);
        assertEquals(10L, summary.getGoogleAccountId());
        assertEquals("dev@example.com", summary.getAccountEmail());
        assertEquals(100L, summary.getTotalFiles());
        assertEquals(104857600L, summary.getTotalStorageBytes());
        assertEquals(5L, summary.getTrashedFiles());
        assertEquals(12L, summary.getTotalFindings());
        assertEquals(8L, summary.getOpenFindings());
        assertEquals(2L, summary.getIgnoredFindings());
        assertEquals(2L, summary.getResolvedFindings());
        assertEquals(3L, summary.getHighSeverityOpenFindings());
        assertEquals(2L, summary.getDuplicateGroups());
        assertEquals(4L, summary.getDuplicateFiles());
        assertEquals(4L, summary.getOldFiles());
        assertEquals(2L, summary.getLargeFiles());
        assertEquals(1L, summary.getExternalShares());
        assertEquals(1L, summary.getPublicFiles());
    }
}
