package com.stanislav.vgt_chat_bot.domain;

import jakarta.persistence.*;
import lombok.Data;


import java.time.Instant;


@Entity
@Table(name = "feedback")
@Data
public class Feedback {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profile_id", nullable = false)
    private EmployeeProfile profile;

    @Column(columnDefinition = "text", nullable = false)
    private String text;

    @Column(nullable = false, length = 16)
    private String sentiment; // NEGATIVE / NEUTRAL / POSITIVE

    @Column(nullable = false)
    private short criticality; // 1..5

    @Column(columnDefinition = "text")
    private String resolution; // совет/решение

    @Column(name = "trello_card_id", length = 64)
    private String trelloCardId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
