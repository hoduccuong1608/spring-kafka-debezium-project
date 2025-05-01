package com.example.sensormonitoring.service;

import com.example.sensormonitoring.model.DebeziumPayload;
import com.example.sensormonitoring.model.SensorReading;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class SensorConsumerService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, SensorReading> redisTemplate;

    public SensorConsumerService(SimpMessagingTemplate messagingTemplate,
            RedisTemplate<String, SensorReading> redisTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.redisTemplate = redisTemplate;
    }

    public void process(DebeziumPayload payload) {
        try {
            SensorReading reading = payload.getAfter();
            if (reading != null) {
                System.out.println("Processed sensor reading with id: " + reading.getId());

                // Lưu vào Redis
                redisTemplate.opsForValue().set("sensor:" + reading.getId(), reading);

                // Gửi qua WebSocket tới frontend qua /topic/sensors
                messagingTemplate.convertAndSend("/topic/sensors", reading);
            } else {
                System.out.println("No sensor reading data in payload (after is null)");
            }
        } catch (Exception e) {
            System.err.println("Error processing payload: " + e.getMessage());
            e.printStackTrace();
        }
    }
}