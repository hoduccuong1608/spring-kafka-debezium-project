package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.sql.Date;

@Entity
@Table(name = "error_summary")
@Data
public class ErrorSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Long summaryId;

    @Column(name = "summary_date", nullable = false)
    private Date summaryDate;

    @Column(name = "error_code", length = 50)
    private String errorCode;

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

    @Column(name = "error_count")
    private Long errorCount = 0L;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}