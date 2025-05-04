package com.example.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "production_logs")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "factory_name", length = 100)
    private String factoryName;

    @Column(name = "line_name", length = 100)
    private String lineName;

    @Column(name = "station_name", length = 100)
    private String stationName;

    @Column(name = "shift_code", length = 50)
    private String shiftCode;

    @Column(name = "work_order_name", length = 50)
    private String workOrderName;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "operation", nullable = false, length = 10)
    private String operation;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}