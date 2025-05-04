package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.sql.Date;

@Entity
@Table(name = "production_summary")
@Data
public class ProductionSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Long summaryId;

    @Column(name = "summary_date", nullable = false)
    private Date summaryDate;

    @Column(name = "factory_name", length = 100)
    private String factoryName;

    @Column(name = "line_name", length = 100)
    private String lineName;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "shift_code", length = 50)
    private String shiftCode;

    @Column(name = "work_order_name", length = 50)
    private String workOrderName;

    @Column(name = "output_count")
    private Long outputCount = 0L;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}