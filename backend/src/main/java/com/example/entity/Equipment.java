package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipment")
@Data
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "serial_number", nullable = false, unique = true)
    private String serialNumber;

    @Column(name = "type")
    private String type;

    @Column(name = "station_id")
    private Integer stationId;

    @Column(name = "status")
    private String status;

    @Column(name = "downtime")
    private Long downtime;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}