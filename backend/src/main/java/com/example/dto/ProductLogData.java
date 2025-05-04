package com.example.dto;

import com.example.util.MicrosecondToLocalDateTimeDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public
class ProductLogData {
    private Long id;
    
    @JsonProperty("serial_number")
    private String serialNumber;

    @JsonDeserialize(using = MicrosecondToLocalDateTimeDeserializer.class)
    private LocalDateTime timestamp;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("station_name")
    private String stationName;

    @JsonProperty("line_name")
    private String lineName;

    @JsonProperty("factory_name")
    private String factoryName;

    @JsonProperty("equipment_serial")
    private String equipmentSerial;

    @JsonProperty("worker_name")
    private String workerName;

    @JsonProperty("shift_code")
    private String shiftCode;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("work_order_name")
    private String workOrderName;

    private String status;

    @JsonProperty("created_at")
    @JsonDeserialize(using = MicrosecondToLocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;
}