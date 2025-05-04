package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.sql.Date;

@Entity
@Table(name = "worker_summary")
@Data
public class WorkerSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Long summaryId;

    @Column(name = "summary_date", nullable = false)
    private Date summaryDate;

    @Column(name = "worker_name", length = 100)
    private String workerName;

    @Column(name = "shift_code", length = 50)
    private String shiftCode;

    @Column(name = "output_count")
    private Long outputCount = 0L;

    @Column(name = "error_count")
    private Long errorCount = 0L;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}