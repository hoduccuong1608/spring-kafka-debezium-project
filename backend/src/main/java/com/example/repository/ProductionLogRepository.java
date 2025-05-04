package com.example.repository;

import com.example.entity.ProductionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductionLogRepository extends JpaRepository<ProductionLog, Long> {
    List<ProductionLog> findByEventTimestampBetweenAndFactoryName(LocalDateTime startTimestamp, LocalDateTime endTimestamp, String factoryName);
    List<ProductionLog> findByWorkOrderName(String workOrderName);
}