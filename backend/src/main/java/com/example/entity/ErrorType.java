package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "error_type")
@Data
public class ErrorType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private String description;
}