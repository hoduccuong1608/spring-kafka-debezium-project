package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "material")
@Data
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "material_code")
    private String materialcode;

    private String name;

    private String unit;

    @Column(name = "cost_per_unit")
    private Double costPerUnit;
}