package com.stanislav.vgt_chat_bot.domain;

import jakarta.persistence.*;
import lombok.Data;


import java.time.Instant;

@Entity
@Table(name = "employee_profile")
@Data
public class EmployeeProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Column(name = "telegram_user_id", nullable = false, unique = true)
    private Long telegramUserId;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Role role;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;


    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
