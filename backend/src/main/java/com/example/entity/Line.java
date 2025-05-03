package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "line")
@Data
public class Line {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "factory_id")
    private Long factoryId;
}