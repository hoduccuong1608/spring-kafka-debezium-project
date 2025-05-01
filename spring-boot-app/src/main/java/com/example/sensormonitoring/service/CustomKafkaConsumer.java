package com.example.sensormonitoring.service;

import com.example.sensormonitoring.model.DebeziumMessage;
import com.example.sensormonitoring.model.DebeziumPayload;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@Service
public class CustomKafkaConsumer {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private KafkaConsumer<String, String> consumer;
    private final ObjectMapper objectMapper;
    private final SensorConsumerService sensorConsumerService;

    public CustomKafkaConsumer(SensorConsumerService sensorConsumerService) {
        this.sensorConsumerService = sensorConsumerService;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        // Cấu hình Kafka consumer
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "sensor-processor");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("sensor.public.sensor_readings"));

        // Bắt đầu thread để poll dữ liệu
        new Thread(this::pollMessages).start();
    }

    private void pollMessages() {
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                records.forEach(record -> {
                    try {
                        String message = record.value();
                        System.out.println("Raw message: " + message);

                        // Parse JSON thành DebeziumMessage
                        DebeziumMessage debeziumMessage = objectMapper.readValue(message, DebeziumMessage.class);
                        System.out.println("Parsed DebeziumMessage: " + debeziumMessage);

                        // Lấy payload từ DebeziumMessage
                        DebeziumPayload payload = debeziumMessage.getPayload();
                        if (payload != null) {
                            System.out.println("Extracted payload: " + payload);

                            // Gọi SensorConsumerService để xử lý
                            sensorConsumerService.process(payload);
                        } else {
                            System.out.println("Payload is null");
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing message: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
                consumer.commitSync();
            }
        } catch (Exception e) {
            System.err.println("Error in Kafka consumer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void shutdown() {
        if (consumer != null) {
            consumer.close();
        }
    }
}