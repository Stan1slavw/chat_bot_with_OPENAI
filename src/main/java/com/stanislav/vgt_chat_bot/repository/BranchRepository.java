package com.stanislav.vgt_chat_bot.repository;

import com.stanislav.vgt_chat_bot.domain.Branch;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;


public interface BranchRepository extends JpaRepository<Branch, Integer> {
    Optional<Branch> findByNameIgnoreCase(String name);
}
