package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "factory")
@Data
public class Factory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "factory_name", nullable = false, unique = true)
    private String factoryName;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}