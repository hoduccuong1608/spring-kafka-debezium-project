package com.example.sensormonitoring.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SensorReading {
    private Long id;

    @JsonProperty("sensor_id")
    private String sensorId;

    private String location;
    private Double temperature;
    private Double humidity;
    private String timestamp;

    // Getters, setters, toString
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "SensorReading{" +
                "id=" + id +
                ", sensorId='" + sensorId + '\'' +
                ", location='" + location + '\'' +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}