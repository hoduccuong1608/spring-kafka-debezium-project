package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "work_order")
@Data
public class WorkOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "work_order_name", nullable = false, unique = true)
    private String workOrderName;

    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "quantity_ordered", nullable = false)
    private Integer quantityOrdered;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "status")
    private String status;

    @Column(name = "factory_id")
    private Integer factoryId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}