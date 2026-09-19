package com.example.drivehealth.service;

import com.example.drivehealth.dto.UserRuleRequest;
import com.example.drivehealth.dto.UserRuleResponse;
import com.example.drivehealth.entity.GoogleAccount;
import com.example.drivehealth.entity.RuleType;
import com.example.drivehealth.entity.UserRule;
import com.example.drivehealth.exception.ResourceNotFoundException;
import com.example.drivehealth.repository.GoogleAccountRepository;
import com.example.drivehealth.repository.UserRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Stage 13: Service for managing user-configurable hygiene rules and evaluating them.
 */
@Service
public class UserRuleService {

    private static final Logger log = LoggerFactory.getLogger(UserRuleService.class);

    private final UserRuleRepository userRuleRepository;
    private final GoogleAccountRepository googleAccountRepository;

    public UserRuleService(UserRuleRepository userRuleRepository,
                           GoogleAccountRepository googleAccountRepository) {
        this.userRuleRepository = userRuleRepository;
        this.googleAccountRepository = googleAccountRepository;
    }

    /**
     * Retrieves all user rules for an account.
     */
    @Transactional(readOnly = true)
    public List<UserRuleResponse> getRules(Long googleAccountId) {
        GoogleAccount account = resolveAccount(googleAccountId);
        List<UserRule> rules = userRuleRepository.findByGoogleAccountId(account.getId());
        List<UserRuleResponse> responses = new ArrayList<>();
        for (UserRule rule : rules) {
            responses.add(mapToResponse(rule));
        }
        return responses;
    }

    /**
     * Creates a new user rule.
     */
    @Transactional
    public UserRuleResponse createRule(Long googleAccountId, UserRuleRequest request) {
        if (request == null || request.getRuleType() == null || request.getRuleValue() == null || request.getRuleValue().trim().isEmpty()) {
            throw new IllegalArgumentException("Rule type and non-empty rule value are required.");
        }

        GoogleAccount account = resolveAccount(googleAccountId);
        UserRule rule = new UserRule();
        rule.setGoogleAccount(account);
        rule.setRuleType(request.getRuleType());
        rule.setRuleValue(request.getRuleValue().trim());
        rule.setDescription(request.getDescription());
        rule.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);

        UserRule saved = userRuleRepository.save(rule);
        log.info("Created user rule #{} ({}: {}) for account {}",
                saved.getId(), saved.getRuleType(), saved.getRuleValue(), account.getEmail());
        return mapToResponse(saved);
    }

    /**
     * Deletes a user rule by ID.
     */
    @Transactional
    public void deleteRule(Long ruleId) {
        UserRule rule = userRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("User rule not found with id: " + ruleId));
        userRuleRepository.delete(rule);
        log.info("Deleted user rule #{}", ruleId);
    }

    /**
     * Checks if a parent folder is ignored by any active rule.
     */
    @Transactional(readOnly = true)
    public boolean isFolderIgnored(Long googleAccountId, String parentId) {
        if (parentId == null || parentId.trim().isEmpty() || googleAccountId == null) {
            return false;
        }
        List<UserRule> rules = userRuleRepository.findByGoogleAccountIdAndRuleTypeAndEnabledTrue(
                googleAccountId, RuleType.IGNORE_FOLDER);
        for (UserRule rule : rules) {
            if (rule.getRuleValue().equalsIgnoreCase(parentId.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a MIME type or file extension is ignored by any active rule.
     */
    @Transactional(readOnly = true)
    public boolean isMimeTypeIgnored(Long googleAccountId, String mimeType) {
        if (mimeType == null || mimeType.trim().isEmpty() || googleAccountId == null) {
            return false;
        }
        List<UserRule> rules = userRuleRepository.findByGoogleAccountIdAndRuleTypeAndEnabledTrue(
                googleAccountId, RuleType.IGNORE_MIME_TYPE);
        for (UserRule rule : rules) {
            String val = rule.getRuleValue().trim();
            if (val.equalsIgnoreCase(mimeType) || mimeType.toLowerCase().contains(val.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the effective large file threshold (bytes).
     * If user configured a LARGE_FILE_THRESHOLD rule, uses that; otherwise uses defaultThreshold.
     */
    @Transactional(readOnly = true)
    public long getEffectiveLargeFileThreshold(Long googleAccountId, long defaultThreshold) {
        if (googleAccountId == null) {
            return defaultThreshold;
        }
        List<UserRule> rules = userRuleRepository.findByGoogleAccountIdAndRuleTypeAndEnabledTrue(
                googleAccountId, RuleType.LARGE_FILE_THRESHOLD);
        if (!rules.isEmpty()) {
            try {
                long custom = Long.parseLong(rules.get(0).getRuleValue().trim());
                if (custom > 0) {
                    return custom;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultThreshold;
    }

    /**
     * Retrieves the effective old file threshold (years).
     * If user configured an OLD_FILE_YEARS rule, uses that; otherwise uses defaultYears.
     */
    @Transactional(readOnly = true)
    public int getEffectiveOldFileYears(Long googleAccountId, int defaultYears) {
        if (googleAccountId == null) {
            return defaultYears;
        }
        List<UserRule> rules = userRuleRepository.findByGoogleAccountIdAndRuleTypeAndEnabledTrue(
                googleAccountId, RuleType.OLD_FILE_YEARS);
        if (!rules.isEmpty()) {
            try {
                int custom = Integer.parseInt(rules.get(0).getRuleValue().trim());
                if (custom > 0) {
                    return custom;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultYears;
    }

    private GoogleAccount resolveAccount(Long googleAccountId) {
        if (googleAccountId != null) {
            return googleAccountRepository.findById(googleAccountId)
                    .orElseThrow(() -> new ResourceNotFoundException("Google account not found with id: " + googleAccountId));
        }

        List<GoogleAccount> accounts = googleAccountRepository.findAll();
        if (accounts.isEmpty()) {
            throw new ResourceNotFoundException("No connected Google account found. Please connect your Google Drive first.");
        }
        return accounts.get(0);
    }

    private UserRuleResponse mapToResponse(UserRule rule) {
        return new UserRuleResponse(
                rule.getId(),
                rule.getGoogleAccountId(),
                rule.getRuleType(),
                rule.getRuleValue(),
                rule.getDescription(),
                rule.isEnabled(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }
}
