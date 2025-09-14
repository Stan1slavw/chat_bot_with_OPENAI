package com.stanislav.vgt_chat_bot.Service;

import com.stanislav.vgt_chat_bot.domain.*;
import com.stanislav.vgt_chat_bot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ProfileService {
    private final EmployeeProfileRepository profileRepo;
    private final BranchRepository branchRepo;

    public EmployeeProfile findByTelegramId(Long tgId) {
        return profileRepo.findByTelegramUserId(tgId).orElse(null);
    }

    public EmployeeProfile getRequired(Long tgId) {
        return profileRepo.findByTelegramUserId(tgId)
                .orElseThrow(() -> new IllegalStateException("Профіль не знайдено. Відправте /start для налаштування."));
    }

    @Transactional
    public EmployeeProfile ensureProfile(Long tgId, Role role, String branchName) {
        var prof = profileRepo.findByTelegramUserId(tgId).orElseGet(EmployeeProfile::new);
        prof.setTelegramUserId(tgId);
        prof.setRole(role);
        var branch = branchRepo.findByNameIgnoreCase(branchName)
                .orElseGet(() -> {
                    var b = new Branch();
                    b.setName(branchName);
                    return branchRepo.save(b);
                });
        prof.setBranch(branch);
        return profileRepo.save(prof);
    }
}
