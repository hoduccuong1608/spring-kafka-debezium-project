-- Tạo bảng sensor_readings
CREATE TABLE sensor_readings (
                                 id BIGSERIAL PRIMARY KEY,
                                 sensor_id VARCHAR(50),
                                 location VARCHAR(50),
                                 temperature FLOAT,
                                 humidity FLOAT,
                                 timestamp TIMESTAMPTZ DEFAULT NOW()
);

-- Tạo publication cho Debezium
CREATE PUBLICATION sensor_pub FOR TABLE sensor_readings;

   -- Tạo index để tối ưu truy vấn
CREATE INDEX idx_sensor_readings_timestamp ON sensor_readings (timestamp);