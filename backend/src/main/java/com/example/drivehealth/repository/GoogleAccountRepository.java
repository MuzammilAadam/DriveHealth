package com.example.drivehealth.repository;

import com.example.drivehealth.entity.GoogleAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoogleAccountRepository extends JpaRepository<GoogleAccount, Long> {

    Optional<GoogleAccount> findByGoogleUserId(String googleUserId);

    Optional<GoogleAccount> findByEmail(String email);

    List<GoogleAccount> findByUserId(Long userId);
}
