package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "product")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code")
    private String productcode;

    private String name;

    private String version;

    private String category; // Phân loại sản phẩm

    @Column(name = "standard_production_time")
    private Long standardProductionTime; // Thời gian sản xuất tiêu chuẩn (phút)
}