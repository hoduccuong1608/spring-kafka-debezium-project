package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "station")
@Data
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "station_name", nullable = false)
    private String stationName;

    @Column(name = "line_id")
    private Integer lineId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}