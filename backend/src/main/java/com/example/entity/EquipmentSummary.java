package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.sql.Date;

@Entity
@Table(name = "equipment_summary")
@Data
public class EquipmentSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Long summaryId;

    @Column(name = "summary_date", nullable = false)
    private Date summaryDate;

    @Column(name = "equipment_serial", length = 50)
    private String equipmentSerial;

    @Column(name = "running_seconds")
    private Long runningSeconds = 0L;

    @Column(name = "stopped_seconds")
    private Long stoppedSeconds = 0L;

    @Column(name = "maintenance_seconds")
    private Long maintenanceSeconds = 0L;

    @Column(name = "error_count")
    private Long errorCount = 0L;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}