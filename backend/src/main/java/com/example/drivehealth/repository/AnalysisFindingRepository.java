package com.example.drivehealth.repository;

import com.example.drivehealth.entity.AnalysisFinding;
import com.example.drivehealth.entity.FindingStatus;
import com.example.drivehealth.entity.FindingType;
import com.example.drivehealth.entity.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalysisFindingRepository extends JpaRepository<AnalysisFinding, Long> {

    /**
     * Standard Spring Data JPA query using property traversal 'GoogleAccount_Id'.
     */
    List<AnalysisFinding> findByGoogleAccount_Id(Long googleAccountId);

    List<AnalysisFinding> findByGoogleAccount_IdAndStatus(Long googleAccountId, FindingStatus status);

    List<AnalysisFinding> findByGoogleAccount_IdAndFindingType(Long googleAccountId, FindingType findingType);

    long countByGoogleAccount_Id(Long googleAccountId);

    long countByGoogleAccount_IdAndStatus(Long googleAccountId, FindingStatus status);

    /**
     * Eagerly loads findings and their associated DriveFile in a single query
     * to prevent N+1 query overhead.
     */
    @Query("SELECT f FROM AnalysisFinding f " +
           "LEFT JOIN FETCH f.driveFile " +
           "WHERE f.googleAccount.id = :googleAccountId " +
           "ORDER BY f.createdAt DESC")
    List<AnalysisFinding> findByGoogleAccountIdWithFile(@Param("googleAccountId") Long googleAccountId);

    /**
     * Deletes findings of a specific type for an account before re-running an analyzer.
     */
    @Modifying
    @Query("DELETE FROM AnalysisFinding f WHERE f.googleAccount.id = :googleAccountId AND f.findingType = :findingType")
    void deleteByGoogleAccountIdAndFindingType(@Param("googleAccountId") Long googleAccountId,
                                              @Param("findingType") FindingType findingType);

    /**
     * Dynamic filtered query for findings.
     */
    @Query("SELECT f FROM AnalysisFinding f " +
           "LEFT JOIN FETCH f.driveFile " +
           "WHERE f.googleAccount.id = :googleAccountId " +
           "AND (:findingType IS NULL OR f.findingType = :findingType) " +
           "AND (:severity IS NULL OR f.severity = :severity) " +
           "AND (:status IS NULL OR f.status = :status) " +
           "ORDER BY f.createdAt DESC")
    List<AnalysisFinding> findFiltered(@Param("googleAccountId") Long googleAccountId,
                                      @Param("findingType") FindingType findingType,
                                      @Param("severity") Severity severity,
                                      @Param("status") FindingStatus status);
}
