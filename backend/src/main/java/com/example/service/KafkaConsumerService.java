package com.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumerService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    public KafkaConsumerService(RedisTemplate<String, Object> redisTemplate, SimpMessagingTemplate messagingTemplate) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "dbz_.public.product_log", groupId = "product_log_group")
    public void listenProductLog(ConsumerRecord<String, JsonNode> record) {
        JsonNode payload = record.value().get("payload");
        if (payload != null) {
            JsonNode after = payload.get("after");
            if (after != null) {
                log.info("Data after: {}", after);
                redisTemplate.opsForValue().set("product_log:" + after.get("id").asText(), after.toString());
                messagingTemplate.convertAndSend("/topic/product_log", after);
            } else {
                log.warn("No 'after' field in the payload: {}", payload);
            }
        } else {
            log.warn("No 'payload' in  No 'payload' in the record: {}", record);
        }
    }

}
