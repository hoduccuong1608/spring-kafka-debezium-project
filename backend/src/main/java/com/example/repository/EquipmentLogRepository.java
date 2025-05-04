package com.example.repository;

import com.example.entity.EquipmentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EquipmentLogRepository extends JpaRepository<EquipmentLog, Long> {
    List<EquipmentLog> findByEventTimestampBetweenAndEquipmentSerial(LocalDateTime startTimestamp, LocalDateTime endTimestamp, String equipmentSerial);
    List<EquipmentLog> findByErrorCode(String errorCode);
}