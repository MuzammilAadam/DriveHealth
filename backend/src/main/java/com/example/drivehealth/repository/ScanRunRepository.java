package com.example.drivehealth.repository;

import com.example.drivehealth.entity.ScanRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScanRunRepository extends JpaRepository<ScanRun, Long> {

    @Query("SELECT s FROM ScanRun s WHERE s.googleAccount.id = :accountId ORDER BY s.startedAt DESC")
    List<ScanRun> findByGoogleAccountIdOrderByStartedAtDesc(@Param("accountId") Long googleAccountId);

    List<ScanRun> findAllByOrderByStartedAtDesc();

    @Query("SELECT s FROM ScanRun s WHERE s.googleAccount.id = :accountId ORDER BY s.startedAt DESC")
    List<ScanRun> findTopListByGoogleAccountIdOrderByStartedAtDesc(@Param("accountId") Long googleAccountId);

    Optional<ScanRun> findTopByGoogleAccount_IdOrderByStartedAtDesc(Long googleAccountId);
}
