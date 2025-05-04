package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "worker")
@Data
public class Worker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "worker_name", nullable = false, unique = true)
    private String workerName;

    @Column(name = "role")
    private String role;

    @Column(name = "department")
    private String department;

    @Column(name = "skill_level")
    private String skillLevel;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}