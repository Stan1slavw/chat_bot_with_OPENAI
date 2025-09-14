package com.stanislav.vgt_chat_bot.repository;

import com.stanislav.vgt_chat_bot.domain.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;


public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, Integer> {
    Optional<EmployeeProfile> findByTelegramUserId(Long telegramUserId);
}
