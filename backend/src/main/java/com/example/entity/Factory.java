package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "factory")
@Data
public class Factory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String location;
}