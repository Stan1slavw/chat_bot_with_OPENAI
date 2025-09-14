package com.stanislav.vgt_chat_bot.controller;

import com.stanislav.vgt_chat_bot.Service.FeedbackQueryService;
import com.stanislav.vgt_chat_bot.domain.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final FeedbackQueryService query;

    @GetMapping("/feedback")
    public String list(@RequestParam(name = "branch", required = false) Optional<String> branch,
                       @RequestParam(name = "role",   required = false) Optional<Role> role,
                       @RequestParam(name = "crit",   required = false) Optional<Short> crit,
                       Model model) {
        model.addAttribute("items", query.find(branch, role, crit));
        model.addAttribute("branch", branch.orElse(""));
        model.addAttribute("crit", crit.orElse(null));
        model.addAttribute("role", role.orElse(null));
        return "admin/feedback_list";
    }
}
