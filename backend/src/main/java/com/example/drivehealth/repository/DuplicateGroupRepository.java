package com.example.drivehealth.repository;

import com.example.drivehealth.entity.DuplicateGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DuplicateGroupRepository extends JpaRepository<DuplicateGroup, Long> {

    /**
     * Finds all duplicate groups for a specific Google Account.
     * Uses JOIN FETCH to efficiently load groupFiles and their associated DriveFile records
     * in a single query, avoiding N+1 SELECT statements.
     */
    @Query("SELECT DISTINCT dg FROM DuplicateGroup dg " +
           "LEFT JOIN FETCH dg.groupFiles dgf " +
           "LEFT JOIN FETCH dgf.driveFile df " +
           "WHERE dg.googleAccount.id = :googleAccountId")
    List<DuplicateGroup> findByGoogleAccountIdWithFiles(@Param("googleAccountId") Long googleAccountId);

    /**
     * Standard Spring Data JPA query using property traversal 'GoogleAccount_Id'.
     */
    List<DuplicateGroup> findByGoogleAccount_Id(Long googleAccountId);

    long countByGoogleAccount_Id(Long googleAccountId);

    /**
     * Clears existing duplicate groups for an account before a fresh analysis run.
     */
    @Modifying
    @Query("DELETE FROM DuplicateGroup dg WHERE dg.googleAccount.id = :googleAccountId")
    void deleteByGoogleAccountId(@Param("googleAccountId") Long googleAccountId);
}

