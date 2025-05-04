package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "error_logs")
@Data
public class ErrorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "error_code", nullable = false, length = 50)
    private String errorCode;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "station_name", length = 100)
    private String stationName;

    @Column(name = "equipment_serial", length = 50)
    private String equipmentSerial;

    @Column(name = "worker_name", length = 100)
    private String workerName;

    @Column(name = "shift_code", length = 50)
    private String shiftCode;

    @Column(name = "operation", nullable = false, length = 10)
    private String operation;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}