package com.example.sensormonitoring.controller;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SensorController {

    private final RedisTemplate<String, Object> redisTemplate;

    public SensorController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/sensor/{id}")
    public String getSensorReading(@PathVariable Long id) {
        String key = "sensor:" + id;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : "Sensor reading not found";
    }
}