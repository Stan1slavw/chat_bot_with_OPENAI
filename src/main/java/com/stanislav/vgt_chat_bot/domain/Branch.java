package com.stanislav.vgt_chat_bot.domain;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "branch")
@Data
public class Branch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Column(unique = true, nullable = false, length = 128)
    private String name;
}
