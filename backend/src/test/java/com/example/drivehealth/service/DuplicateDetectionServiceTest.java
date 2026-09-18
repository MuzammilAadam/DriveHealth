package com.example.drivehealth.service;

import com.example.drivehealth.dto.DuplicateResponse;
import com.example.drivehealth.entity.DriveFile;
import com.example.drivehealth.entity.DuplicateGroup;
import com.example.drivehealth.entity.GoogleAccount;
import com.example.drivehealth.entity.User;
import com.example.drivehealth.repository.DriveFileRepository;
import com.example.drivehealth.repository.DuplicateGroupRepository;
import com.example.drivehealth.repository.GoogleAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DuplicateDetectionServiceTest {

    @Mock
    private GoogleAccountRepository googleAccountRepository;

    @Mock
    private DriveFileRepository driveFileRepository;

    @Mock
    private DuplicateGroupRepository duplicateGroupRepository;

    @Mock
    private com.example.drivehealth.repository.AnalysisFindingRepository analysisFindingRepository;

    @InjectMocks
    private DuplicateDetectionService duplicateDetectionService;

    private GoogleAccount testAccount;

    @BeforeEach
    void setUp() {
        User user = new User("developer@example.com", "Dev User");
        user.setId(1L);

        testAccount = new GoogleAccount();
        testAccount.setId(10L);
        testAccount.setUser(user);
        testAccount.setEmail("developer@gmail.com");
    }

    @Test
    void testDetectDuplicates_GroupsExactDuplicatesByMd5Checksum() {
        when(googleAccountRepository.findById(10L)).thenReturn(Optional.of(testAccount));

        // Create sample files:
        // File 1: resume.pdf (MD5 = "abc123", size = 5000)
        DriveFile file1 = new DriveFile();
        file1.setId(101L);
        file1.setName("resume.pdf");
        file1.setGoogleFileId("google-id-1");
        file1.setMd5Checksum("abc123");
        file1.setSize(5000L);
        file1.setGoogleAccount(testAccount);

        // File 2: resume_final.pdf (MD5 = "abc123", size = 5000) -> DUPLICATE of File 1
        DriveFile file2 = new DriveFile();
        file2.setId(102L);
        file2.setName("resume_final.pdf");
        file2.setGoogleFileId("google-id-2");
        file2.setMd5Checksum("abc123");
        file2.setSize(5000L);
        file2.setGoogleAccount(testAccount);

        // File 3: notes.txt (MD5 = "xyz789", size = 1200) -> UNIQUE, not a duplicate
        DriveFile file3 = new DriveFile();
        file3.setId(103L);
        file3.setName("notes.txt");
        file3.setGoogleFileId("google-id-3");
        file3.setMd5Checksum("xyz789");
        file3.setSize(1200L);
        file3.setGoogleAccount(testAccount);

        // File 4: Google Docs document -> null checksum -> ignored
        DriveFile file4 = new DriveFile();
        file4.setId(104L);
        file4.setName("Meeting Minutes");
        file4.setGoogleFileId("google-id-4");
        file4.setMd5Checksum(null);
        file4.setSize(0L);
        file4.setGoogleAccount(testAccount);

        List<DriveFile> mockFiles = new ArrayList<>();
        mockFiles.add(file1);
        mockFiles.add(file2);
        mockFiles.add(file3);
        mockFiles.add(file4);

        when(driveFileRepository.findByGoogleAccount_IdAndTrashedFalse(10L)).thenReturn(mockFiles);

        // Execute duplicate detection
        DuplicateResponse response = duplicateDetectionService.detectDuplicates(10L);

        // Verify old groups were cleared
        verify(duplicateGroupRepository).deleteByGoogleAccountId(10L);

        // Verify duplicate groups were saved
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DuplicateGroup>> captor = ArgumentCaptor.forClass(List.class);
        verify(duplicateGroupRepository).saveAll(captor.capture());

        List<DuplicateGroup> savedGroups = captor.getValue();
        assertEquals(1, savedGroups.size(), "Should find exactly 1 duplicate group");

        DuplicateGroup group = savedGroups.get(0);
        assertEquals("abc123", group.getMd5Checksum());
        assertEquals(100, group.getConfidence());
        assertEquals("Files have the same checksum", group.getReason());
        assertEquals(2, group.getGroupFiles().size(), "Group should contain 2 files");

        // Verify API response DTO
        assertNotNull(response);
        assertEquals(1, response.getTotalDuplicateGroups());
        assertEquals(2, response.getTotalDuplicateFiles());
        assertEquals(5000L, response.getTotalWastedStorageBytes(), "1 extra copy of 5000 bytes = 5000 bytes wasted");
        assertEquals(1, response.getGroups().size());
        assertEquals("resume.pdf", response.getGroups().get(0).getFiles().get(0).getName());
        assertEquals("resume_final.pdf", response.getGroups().get(0).getFiles().get(1).getName());
    }
}
