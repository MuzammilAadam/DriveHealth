package com.example.drivehealth.repository;

import com.example.drivehealth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    List<Permission> findByGoogleAccount_Id(Long googleAccountId);

    List<Permission> findByDriveFile_Id(Long driveFileId);

    Optional<Permission> findByDriveFile_IdAndPermissionId(Long driveFileId, String permissionId);

    // Fetch all permissions for an account where type is "anyone" (public/link-shared)
    @Query("SELECT p FROM Permission p WHERE p.googleAccount.id = :accountId AND p.type = 'anyone'")
    List<Permission> findPublicPermissionsByAccount(@Param("accountId") Long accountId);

    // Fetch non-owner, non-account-domain permissions (potential external shares)
    @Query("SELECT p FROM Permission p WHERE p.googleAccount.id = :accountId " +
           "AND p.type IN ('user', 'group') AND p.role <> 'owner'")
    List<Permission> findNonOwnerUserPermissions(@Param("accountId") Long accountId);

    // Delete all permissions for a specific file (used before re-syncing)
    @Modifying
    @Query("DELETE FROM Permission p WHERE p.driveFile.id = :driveFileId")
    void deleteByDriveFileId(@Param("driveFileId") Long driveFileId);

    // Delete all permissions for an account (full re-sync)
    @Modifying
    @Query("DELETE FROM Permission p WHERE p.googleAccount.id = :accountId")
    void deleteByGoogleAccountId(@Param("accountId") Long accountId);
}
