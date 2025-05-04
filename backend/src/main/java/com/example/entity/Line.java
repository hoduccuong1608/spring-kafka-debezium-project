package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "line")
@Data
public class Line {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "line_name", nullable = false)
    private String lineName;

    @Column(name = "factory_id")
    private Integer factoryId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}