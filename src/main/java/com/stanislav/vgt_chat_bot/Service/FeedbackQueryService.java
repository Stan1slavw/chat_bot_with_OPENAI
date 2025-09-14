package com.stanislav.vgt_chat_bot.Service;

import com.stanislav.vgt_chat_bot.domain.*;
import com.stanislav.vgt_chat_bot.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class FeedbackQueryService {
    private final FeedbackRepository repo;


    public List<Feedback> find(Optional<String> branch, Optional<Role> role, Optional<Short> crit) {
        Specification<Feedback> spec = Specification.where(null);
        if (branch.isPresent()) {
            String b = branch.get().toLowerCase();
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.join("profile").join("branch").get("name")), "%"+b+"%"));
        }
        if (role.isPresent()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.join("profile").get("role"), role.get()));
        }
        if (crit.isPresent()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("criticality"), crit.get()));
        }
        q:
        return repo.findAll(spec);
    }
}
