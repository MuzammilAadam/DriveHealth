package com.example.drivehealth.service;

import com.example.drivehealth.dto.UserRuleRequest;
import com.example.drivehealth.dto.UserRuleResponse;
import com.example.drivehealth.entity.GoogleAccount;
import com.example.drivehealth.entity.RuleType;
import com.example.drivehealth.entity.User;
import com.example.drivehealth.entity.UserRule;
import com.example.drivehealth.repository.GoogleAccountRepository;
import com.example.drivehealth.repository.UserRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRuleServiceTest {

    @Mock
    private UserRuleRepository userRuleRepository;

    @Mock
    private GoogleAccountRepository googleAccountRepository;

    @InjectMocks
    private UserRuleService userRuleService;

    private GoogleAccount testAccount;

    @BeforeEach
    void setUp() {
        User user = new User("dev@example.com", "Dev");
        user.setId(1L);

        testAccount = new GoogleAccount();
        testAccount.setId(10L);
        testAccount.setUser(user);
        testAccount.setEmail("dev@example.com");
    }

    @Test
    void testCreateAndGetRules() {
        when(googleAccountRepository.findById(10L)).thenReturn(Optional.of(testAccount));

        UserRule rule = new UserRule(testAccount, RuleType.IGNORE_FOLDER, "folder-archive-123", "Ignore archive");
        rule.setId(100L);
        when(userRuleRepository.save(any(UserRule.class))).thenReturn(rule);

        UserRuleRequest req = new UserRuleRequest(RuleType.IGNORE_FOLDER, "folder-archive-123", "Ignore archive");
        UserRuleResponse created = userRuleService.createRule(10L, req);

        assertNotNull(created);
        assertEquals(RuleType.IGNORE_FOLDER, created.getRuleType());
        assertEquals("folder-archive-123", created.getRuleValue());

        List<UserRule> rules = new ArrayList<>();
        rules.add(rule);
        when(userRuleRepository.findByGoogleAccountId(10L)).thenReturn(rules);

        List<UserRuleResponse> responses = userRuleService.getRules(10L);
        assertEquals(1, responses.size());
    }

    @Test
    void testRuleEvaluation_FolderAndMimeType() {
        UserRule folderRule = new UserRule(testAccount, RuleType.IGNORE_FOLDER, "archive-123", "desc");
        List<UserRule> folderRules = List.of(folderRule);
        when(userRuleRepository.findByGoogleAccountIdAndRuleTypeAndEnabledTrue(10L, RuleType.IGNORE_FOLDER))
                .thenReturn(folderRules);

        assertTrue(userRuleService.isFolderIgnored(10L, "archive-123"));
        assertFalse(userRuleService.isFolderIgnored(10L, "other-folder"));

        UserRule mimeRule = new UserRule(testAccount, RuleType.IGNORE_MIME_TYPE, "video/mp4", "desc");
        List<UserRule> mimeRules = List.of(mimeRule);
        when(userRuleRepository.findByGoogleAccountIdAndRuleTypeAndEnabledTrue(10L, RuleType.IGNORE_MIME_TYPE))
                .thenReturn(mimeRules);

        assertTrue(userRuleService.isMimeTypeIgnored(10L, "video/mp4"));
        assertFalse(userRuleService.isMimeTypeIgnored(10L, "application/pdf"));
    }

    @Test
    void testEffectiveThresholds() {
        UserRule sizeRule = new UserRule(testAccount, RuleType.LARGE_FILE_THRESHOLD, "1073741824", "1GB");
        when(userRuleRepository.findByGoogleAccountIdAndRuleTypeAndEnabledTrue(10L, RuleType.LARGE_FILE_THRESHOLD))
                .thenReturn(List.of(sizeRule));

        long threshold = userRuleService.getEffectiveLargeFileThreshold(10L, 524288000L);
        assertEquals(1073741824L, threshold);

        UserRule oldRule = new UserRule(testAccount, RuleType.OLD_FILE_YEARS, "4", "4 years");
        when(userRuleRepository.findByGoogleAccountIdAndRuleTypeAndEnabledTrue(10L, RuleType.OLD_FILE_YEARS))
                .thenReturn(List.of(oldRule));

        int years = userRuleService.getEffectiveOldFileYears(10L, 2);
        assertEquals(4, years);
    }
}
