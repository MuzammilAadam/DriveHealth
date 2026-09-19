package com.example.drivehealth.repository;

import com.example.drivehealth.entity.RuleType;
import com.example.drivehealth.entity.UserRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRuleRepository extends JpaRepository<UserRule, Long> {

    @Query("SELECT r FROM UserRule r WHERE r.googleAccount.id = :accountId")
    List<UserRule> findByGoogleAccountId(@Param("accountId") Long googleAccountId);

    @Query("SELECT r FROM UserRule r WHERE r.googleAccount.id = :accountId AND r.enabled = true")
    List<UserRule> findByGoogleAccountIdAndEnabledTrue(@Param("accountId") Long googleAccountId);

    @Query("SELECT r FROM UserRule r WHERE r.googleAccount.id = :accountId AND r.ruleType = :ruleType AND r.enabled = true")
    List<UserRule> findByGoogleAccountIdAndRuleTypeAndEnabledTrue(
            @Param("accountId") Long googleAccountId,
            @Param("ruleType") RuleType ruleType);
}
