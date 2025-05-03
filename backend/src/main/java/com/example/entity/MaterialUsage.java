package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "material_usage")
@Data
public class MaterialUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantity_used")
    private Double quantityused;

    @Column(name = "product_log_id")
    private Long productlogId;

    @Column(name = "material_id")
    private Long materialId;

    private Double cost; // Chi phí vật liệu sử dụng

    private LocalDateTime timestamp; // Thời gian sử dụng
}