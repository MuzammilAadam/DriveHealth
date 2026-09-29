package com.example.drivehealth.repository;

import com.example.drivehealth.entity.DuplicateGroupFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DuplicateGroupFileRepository extends JpaRepository<DuplicateGroupFile, Long> {

    List<DuplicateGroupFile> findByDuplicateGroup_Id(Long duplicateGroupId);

    long countByDuplicateGroup_GoogleAccount_Id(Long googleAccountId);

    @Modifying
    @Query("DELETE FROM DuplicateGroupFile d WHERE d.driveFile.id = :driveFileId")
    void deleteByDriveFileId(@Param("driveFileId") Long driveFileId);
}
