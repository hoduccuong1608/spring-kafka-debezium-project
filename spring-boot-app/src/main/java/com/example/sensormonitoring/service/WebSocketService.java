package com.example.sensormonitoring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.sensormonitoring.model.SensorReading;

@Service
public class WebSocketService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendUpdate(SensorReading reading) {
        messagingTemplate.convertAndSend("/topic/sensors", reading);
    }
}