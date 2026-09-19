package com.example.drivehealth.repository;

import com.example.drivehealth.entity.StorageSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StorageSnapshotRepository extends JpaRepository<StorageSnapshot, Long> {

    @Query("SELECT s FROM StorageSnapshot s WHERE s.googleAccount.id = :accountId ORDER BY s.snapshotTime DESC")
    List<StorageSnapshot> findByGoogleAccountIdOrderBySnapshotTimeDesc(@Param("accountId") Long googleAccountId);

    Optional<StorageSnapshot> findTopByGoogleAccount_IdOrderBySnapshotTimeDesc(Long googleAccountId);
}
