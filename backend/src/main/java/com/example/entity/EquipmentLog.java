package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_logs")
@Data
public class EquipmentLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "equipment_serial", nullable = false, length = 50)
    private String equipmentSerial;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "line_name", length = 100)
    private String lineName;

    @Column(name = "station_name", length = 100)
    private String stationName;

    @Column(name = "operation", nullable = false, length = 10)
    private String operation;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}