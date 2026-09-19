package com.example.drivehealth.service;

import com.example.drivehealth.dto.ScanRunResponse;
import com.example.drivehealth.entity.GoogleAccount;
import com.example.drivehealth.entity.ScanRun;
import com.example.drivehealth.entity.User;
import com.example.drivehealth.repository.GoogleAccountRepository;
import com.example.drivehealth.repository.ScanRunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScanRunServiceTest {

    @Mock
    private ScanRunRepository scanRunRepository;

    @Mock
    private GoogleAccountRepository googleAccountRepository;

    @InjectMocks
    private ScanRunService scanRunService;

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
    void testStartAndCompleteScanRun() {
        ScanRun run = new ScanRun(testAccount, "FULL");
        run.setId(1L);
        when(scanRunRepository.save(any(ScanRun.class))).thenReturn(run);

        ScanRun started = scanRunService.startScanRun(testAccount, "FULL");
        assertNotNull(started);
        assertEquals("IN_PROGRESS", started.getStatus());

        scanRunService.completeScanRun(started, 150, 10, 5, 2);
        assertEquals("COMPLETED", started.getStatus());
        assertEquals(150, started.getFilesScanned());
        assertEquals(10, started.getNewFiles());
        assertEquals(5, started.getUpdatedFiles());
        assertEquals(2, started.getFindingsCreated());
    }

    @Test
    void testFailScanRun() {
        ScanRun run = new ScanRun(testAccount, "FULL");
        run.setId(2L);
        when(scanRunRepository.save(any(ScanRun.class))).thenReturn(run);

        ScanRun started = scanRunService.startScanRun(testAccount, "FULL");
        scanRunService.failScanRun(started, "Network timeout");

        assertEquals("FAILED", started.getStatus());
        assertEquals("Network timeout", started.getErrorMessage());
    }

    @Test
    void testGetScans() {
        ScanRun run = new ScanRun(testAccount, "FULL");
        run.setId(1L);
        run.setStatus("COMPLETED");
        when(scanRunRepository.findByGoogleAccountIdOrderByStartedAtDesc(10L)).thenReturn(List.of(run));

        List<ScanRunResponse> scans = scanRunService.getScans(10L);
        assertEquals(1, scans.size());
        assertEquals(1L, scans.get(0).getId());
        assertEquals("COMPLETED", scans.get(0).getStatus());
    }
}
