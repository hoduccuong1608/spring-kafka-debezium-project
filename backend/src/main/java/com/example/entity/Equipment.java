package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "equipment")
@Data
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_number")
    private String serialnumber;

    private String type;

    @Column(name = "station_id")
    private Long stationId;

    private String status; // ACTIVE, BROKEN, MAINTENANCE

    private Long downtime; // Thời gian ngừng hoạt động (phút)
}