package com.example.sensormonitoring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.sensormonitoring.model.SensorReading;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void saveToCache(String key, SensorReading reading) {
        redisTemplate.opsForValue().set(key + ":" + reading.getId(), reading, 5, TimeUnit.MINUTES);
    }

    public SensorReading getFromCache(String key, Long id) {
        return (SensorReading) redisTemplate.opsForValue().get(key + ":" + id);
    }
}