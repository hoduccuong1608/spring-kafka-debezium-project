package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table(name = "error_log")
public class ErrorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_log_id")
    private Long productLogId;

    @Column(name = "error_type_id")
    private Long errorTypeId;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    private String note;
}
