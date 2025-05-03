package com.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;

@Entity
@Table(name = "shift")
@Data
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shift_code")
    private String shiftcode;

    @Column(name = "start_time")
    private LocalTime starttime;

    @Column(name = "end_time")
    private LocalTime endtime;
}