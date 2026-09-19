package com.example.drivehealth.service;

import com.example.drivehealth.dto.StorageSnapshotResponse;
import com.example.drivehealth.entity.GoogleAccount;
import com.example.drivehealth.entity.StorageSnapshot;
import com.example.drivehealth.entity.User;
import com.example.drivehealth.repository.DriveFileRepository;
import com.example.drivehealth.repository.GoogleAccountRepository;
import com.example.drivehealth.repository.StorageSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageSnapshotServiceTest {

    @Mock
    private StorageSnapshotRepository storageSnapshotRepository;

    @Mock
    private DriveFileRepository driveFileRepository;

    @Mock
    private GoogleAccountRepository googleAccountRepository;

    @InjectMocks
    private StorageSnapshotService storageSnapshotService;

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
    void testRecordSnapshot() {
        when(driveFileRepository.countByGoogleAccountId(10L)).thenReturn(50L);
        when(driveFileRepository.sumSizeByGoogleAccountId(10L)).thenReturn(5000000L);
        when(driveFileRepository.countByGoogleAccountIdAndTrashedTrue(10L)).thenReturn(5L);
        when(driveFileRepository.sumTrashedSizeByGoogleAccountId(10L)).thenReturn(500000L);

        StorageSnapshot snapshot = new StorageSnapshot(testAccount, 50, 5000000, 5, 500000, 45, 4500000);
        snapshot.setId(1L);
        when(storageSnapshotRepository.save(any(StorageSnapshot.class))).thenReturn(snapshot);

        StorageSnapshot recorded = storageSnapshotService.recordSnapshot(testAccount);
        assertNotNull(recorded);
        assertEquals(50L, recorded.getTotalFiles());
        assertEquals(5000000L, recorded.getTotalStorageBytes());
        assertEquals(5L, recorded.getTrashedFiles());
        assertEquals(45L, recorded.getActiveFiles());
        assertEquals(4500000L, recorded.getActiveStorageBytes());
        verify(storageSnapshotRepository).save(any(StorageSnapshot.class));
    }

    @Test
    void testGetSnapshots() {
        when(googleAccountRepository.findById(10L)).thenReturn(Optional.of(testAccount));

        StorageSnapshot s = new StorageSnapshot(testAccount, 50, 5000000, 5, 500000, 45, 4500000);
        s.setId(1L);
        s.setSnapshotTime(LocalDateTime.now());
        when(storageSnapshotRepository.findByGoogleAccountIdOrderBySnapshotTimeDesc(10L)).thenReturn(List.of(s));

        List<StorageSnapshotResponse> list = storageSnapshotService.getSnapshots(10L);
        assertEquals(1, list.size());
        assertEquals(50L, list.get(0).getTotalFiles());
    }
}
