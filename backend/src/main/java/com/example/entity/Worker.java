package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "worker")
@Data
public class Worker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String role;

    private String department; // Bộ phận

    @Column(name = "skill_level")
    private String skillLevel; // Trình độ kỹ năng
}