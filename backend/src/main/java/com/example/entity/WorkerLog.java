package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "worker_logs")
@Data
public class WorkerLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "worker_name", nullable = false, length = 100)
    private String workerName;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @Column(name = "shift_code", length = 50)
    private String shiftCode;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "output_count")
    private Integer outputCount = 1;

    @Column(name = "operation", nullable = false, length = 10)
    private String operation;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}