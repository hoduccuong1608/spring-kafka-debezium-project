package com.example.sensormonitoring.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DebeziumMessage {
    private Object schema; // Có thể dùng Object vì chúng ta không cần xử lý schema
    private DebeziumPayload payload;

    // Getters, setters
    public Object getSchema() {
        return schema;
    }

    public void setSchema(Object schema) {
        this.schema = schema;
    }

    public DebeziumPayload getPayload() {
        return payload;
    }

    public void setPayload(DebeziumPayload payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "DebeziumMessage{" +
                "schema=" + schema +
                ", payload=" + payload +
                '}';
    }
}