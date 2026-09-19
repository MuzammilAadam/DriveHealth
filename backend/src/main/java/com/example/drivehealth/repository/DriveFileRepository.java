package com.example.drivehealth.repository;

import com.example.drivehealth.entity.DriveFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriveFileRepository extends JpaRepository<DriveFile, Long> {

    // =========================================================================
    // WHY THE PREVIOUS CODE FAILED (Root Cause Explanation):
    // 
    // In DriveFile.java, the relationship field is:
    //     @ManyToOne(fetch = FetchType.LAZY)
    //     @JoinColumn(name = "google_account_id", nullable = false)
    //     private GoogleAccount googleAccount;
    //
    // Notice the Java entity property is named "googleAccount" (an object),
    // NOT "googleAccountId" (a primitive/wrapper ID).
    //
    // When Spring Boot started up, Spring Data JPA tried to derive a query from:
    //     long countByGoogleAccountId(Long googleAccountId);
    // Hibernate looked for a direct attribute named "googleAccountId" on DriveFile.
    // Because no such attribute exists, application startup crashed with:
    //     "Could not resolve attribute 'googleAccountId' of 'DriveFile'"
    //
    // HOW IT IS FIXED:
    // 1. Spring Data JPA property traversal convention uses an underscore '_'
    //    to traverse into the nested entity's ID:
    //    'countByGoogleAccount_Id' -> translates to: driveFile.googleAccount.id = ?
    // 2. We also provide explicit @Query methods for 'countByGoogleAccountId'
    //    and 'findByGoogleAccountId' so JPQL explicitly queries 'f.googleAccount.id',
    //    preventing Hibernate from guessing the property name incorrectly.
    // =========================================================================

    /**
     * Standard Spring Data JPA nested traversal using 'GoogleAccount_Id' (googleAccount.id).
     */
    Optional<DriveFile> findByGoogleAccount_IdAndGoogleFileId(Long googleAccountId, String googleFileId);

    List<DriveFile> findByGoogleAccount_Id(Long googleAccountId);

    Page<DriveFile> findByGoogleAccount_Id(Long googleAccountId, Pageable pageable);

    List<DriveFile> findByGoogleAccount_IdAndTrashedFalse(Long googleAccountId);

    long countByGoogleAccount_Id(Long googleAccountId);

    // -------------------------------------------------------------------------
    // Explicit @Query aliases:
    // These methods explicitly tell Hibernate the exact JPQL path (f.googleAccount.id),
    // ensuring methods without an underscore also work without any mapping ambiguity.
    // -------------------------------------------------------------------------

    @Query("SELECT f FROM DriveFile f WHERE f.googleAccount.id = :googleAccountId AND f.googleFileId = :googleFileId")
    Optional<DriveFile> findByGoogleAccountIdAndGoogleFileId(@Param("googleAccountId") Long googleAccountId,
                                                            @Param("googleFileId") String googleFileId);

    @Query("SELECT f FROM DriveFile f WHERE f.googleAccount.id = :googleAccountId")
    List<DriveFile> findByGoogleAccountId(@Param("googleAccountId") Long googleAccountId);

    @Query("SELECT f FROM DriveFile f WHERE f.googleAccount.id = :googleAccountId")
    Page<DriveFile> findByGoogleAccountId(@Param("googleAccountId") Long googleAccountId, Pageable pageable);

    @Query("SELECT f FROM DriveFile f WHERE f.googleAccount.id = :googleAccountId AND f.trashed = false")
    List<DriveFile> findByGoogleAccountIdAndTrashedFalse(@Param("googleAccountId") Long googleAccountId);

    @Query("SELECT COUNT(f) FROM DriveFile f WHERE f.googleAccount.id = :googleAccountId")
    long countByGoogleAccountId(@Param("googleAccountId") Long googleAccountId);

    @Query("SELECT COALESCE(SUM(f.size), 0) FROM DriveFile f WHERE f.googleAccount.id = :googleAccountId")
    long sumSizeByGoogleAccountId(@Param("googleAccountId") Long googleAccountId);

    @Query("SELECT COUNT(f) FROM DriveFile f WHERE f.googleAccount.id = :googleAccountId AND f.trashed = true")
    long countByGoogleAccountIdAndTrashedTrue(@Param("googleAccountId") Long googleAccountId);

    @Query("SELECT COALESCE(SUM(f.size), 0) FROM DriveFile f WHERE f.googleAccount.id = :googleAccountId AND f.trashed = true")
    long sumTrashedSizeByGoogleAccountId(@Param("googleAccountId") Long googleAccountId);

    void deleteByGoogleAccount_IdAndGoogleFileId(Long googleAccountId, String googleFileId);
}
