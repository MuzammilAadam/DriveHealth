package com.example.drivehealth.service;

import com.example.drivehealth.dto.DriveFileDto;
import com.example.drivehealth.dto.DriveFilesPageResponse;
import com.example.drivehealth.dto.ScanSummaryResponse;
import com.example.drivehealth.entity.DriveFile;
import com.example.drivehealth.entity.GoogleAccount;
import com.example.drivehealth.entity.User;
import com.example.drivehealth.repository.DriveFileRepository;
import com.example.drivehealth.repository.GoogleAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleDriveServiceTest {

    @Mock
    private GoogleAccountRepository googleAccountRepository;

    @Mock
    private DriveFileRepository driveFileRepository;

    @Mock
    private GoogleOAuthService googleOAuthService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GoogleDriveService googleDriveService;

    private GoogleAccount testAccount;

    @BeforeEach
    void setUp() {
        User user = new User("developer@example.com", "Dev User");
        user.setId(1L);

        testAccount = new GoogleAccount();
        testAccount.setId(10L);
        testAccount.setUser(user);
        testAccount.setEmail("developer@gmail.com");
        testAccount.setGoogleUserId("google-sub-123");
    }

    @Test
    void testScanAndSyncFiles_HandlesNewAndUpdatedFilesCorrectly() {
        when(googleAccountRepository.findById(10L)).thenReturn(Optional.of(testAccount));

        // Existing local file in MySQL (id="file-existing")
        DriveFile existingFile = new DriveFile();
        existingFile.setId(100L);
        existingFile.setGoogleAccount(testAccount);
        existingFile.setGoogleFileId("file-existing");
        existingFile.setName("OldName.pdf");
        existingFile.setSize(1000L);

        List<DriveFile> initialDbFiles = new ArrayList<>();
        initialDbFiles.add(existingFile);
        when(driveFileRepository.findByGoogleAccount_Id(10L)).thenReturn(initialDbFiles);

        // Prepare mock remote files from Google Drive API
        // 1 updated file + 1 new file
        List<DriveFileDto> remoteFiles = new ArrayList<>();
        remoteFiles.add(new DriveFileDto(
                "file-existing", "UpdatedName.pdf", "application/pdf", 1200L,
                LocalDateTime.now().minusDays(10), LocalDateTime.now(), "root",
                "http://drive/view/existing", "md5-updated", false, "developer@gmail.com"
        ));
        remoteFiles.add(new DriveFileDto(
                "file-brand-new", "Report.docx", "application/vnd.google-apps.document", 2048L,
                LocalDateTime.now(), LocalDateTime.now(), "root",
                "http://drive/view/brand-new", "md5-new", false, "developer@gmail.com"
        ));

        // Create a spy on GoogleDriveService to mock fetchAllFiles
        GoogleDriveService spyService = org.mockito.Mockito.spy(googleDriveService);
        doReturn(remoteFiles).when(spyService).fetchAllFiles(10L);

        // Execute scan
        ScanSummaryResponse summary = spyService.scanAndSyncFiles(10L);

        // Assertions
        assertNotNull(summary);
        assertEquals(2, summary.getFilesScanned(), "Should have scanned 2 files total");
        assertEquals(1, summary.getNewFiles(), "Should have detected 1 new file");
        assertEquals(1, summary.getUpdatedFiles(), "Should have detected 1 updated file");

        // Verify batch save captured both files
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DriveFile>> captor = ArgumentCaptor.forClass(List.class);
        verify(driveFileRepository).saveAll(captor.capture());

        List<DriveFile> savedFiles = captor.getValue();
        assertEquals(2, savedFiles.size());

        // Verify existing file got updated fields
        DriveFile updated = savedFiles.stream()
                .filter(f -> f.getGoogleFileId().equals("file-existing"))
                .findFirst().orElseThrow();
        assertEquals("UpdatedName.pdf", updated.getName());
        assertEquals(1200L, updated.getSize());
        assertEquals("md5-updated", updated.getMd5Checksum());

        // Verify new file got set up
        DriveFile newlyCreated = savedFiles.stream()
                .filter(f -> f.getGoogleFileId().equals("file-brand-new"))
                .findFirst().orElseThrow();
        assertEquals("Report.docx", newlyCreated.getName());
        assertEquals(testAccount, newlyCreated.getGoogleAccount());
        assertEquals("md5-new", newlyCreated.getMd5Checksum());

        // Verify account sync timestamp was updated
        verify(googleAccountRepository).save(testAccount);
        assertNotNull(testAccount.getLastSyncedAt());
    }
}
