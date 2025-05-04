package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "product_code", nullable = false, unique = true)
    private String productCode;

    @Column(name = "product_name", nullable = false, unique = true)
    private String productName;

    @Column(name = "version")
    private String version;

    @Column(name = "category")
    private String category;

    @Column(name = "standard_production_time")
    private Long standardProductionTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}