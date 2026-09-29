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

    List<AnalysisFinding> findByGoogleAccount_Id(Long googleAccountId);

    List<AnalysisFinding> findByGoogleAccount_IdAndStatus(Long googleAccountId, FindingStatus status);

    List<AnalysisFinding> findByGoogleAccount_IdAndFindingType(Long googleAccountId, FindingType findingType);

    long countByGoogleAccount_Id(Long googleAccountId);

    long countByGoogleAccount_IdAndStatus(Long googleAccountId, FindingStatus status);

    long countByGoogleAccount_IdAndStatusAndSeverity(Long googleAccountId, FindingStatus status, Severity severity);

    long countByGoogleAccount_IdAndFindingType(Long googleAccountId, FindingType findingType);

    @Query("SELECT f FROM AnalysisFinding f " +
           "LEFT JOIN FETCH f.driveFile " +
           "WHERE f.googleAccount.id = :googleAccountId " +
           "ORDER BY f.createdAt DESC")
    List<AnalysisFinding> findByGoogleAccountIdWithFile(@Param("googleAccountId") Long googleAccountId);

    @Modifying
    @Query("DELETE FROM AnalysisFinding f WHERE f.googleAccount.id = :googleAccountId AND f.findingType = :findingType")
    void deleteByGoogleAccountIdAndFindingType(@Param("googleAccountId") Long googleAccountId,
                                               @Param("findingType") FindingType findingType);

    @Modifying
    @Query("DELETE FROM AnalysisFinding f WHERE f.driveFile.id = :driveFileId")
    void deleteByDriveFileId(@Param("driveFileId") Long driveFileId);

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
