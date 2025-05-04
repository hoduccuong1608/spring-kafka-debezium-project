package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_log")
@Data
public class ProductLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "serial_number", length = 50)
    private String serialNumber;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "station_name", length = 100)
    private String stationName;

    @Column(name = "line_name", length = 100)
    private String lineName;

    @Column(name = "factory_name", length = 100)
    private String factoryName;

    @Column(name = "equipment_serial", length = 50)
    private String equipmentSerial;

    @Column(name = "worker_name", length = 100)
    private String workerName;

    @Column(name = "shift_code", length = 50)
    private String shiftCode;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "work_order_name", length = 50)
    private String workOrderName;

    @Column(name = "status", length = 50)
    private String status;

    private LocalDateTime createdAt;
}