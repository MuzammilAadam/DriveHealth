package com.example.drivehealth.repository;

import com.example.drivehealth.entity.DuplicateGroupFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DuplicateGroupFileRepository extends JpaRepository<DuplicateGroupFile, Long> {

    List<DuplicateGroupFile> findByDuplicateGroup_Id(Long duplicateGroupId);
}
