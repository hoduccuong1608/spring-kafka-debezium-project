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
    private Long id;

    @Column(name = "serial_number")
    private String serialNumber;

    private LocalDateTime timestamp;

    private String productName;      // Tên sản phẩm
    private String stationName;      // Tên trạm
    private String lineName;         // Tên dây chuyền
    private String factoryName;      // Tên nhà máy
    private String equipmentSerial;  // Số serial thiết bị
    private String workerName;       // Tên công nhân
    private String shiftCode;        // Mã ca làm việc
    private String errorCode;        // Mã lỗi
    private String workOrderName;    // Tên lệnh sản xuất
    private String status;           // Trạng thái
}