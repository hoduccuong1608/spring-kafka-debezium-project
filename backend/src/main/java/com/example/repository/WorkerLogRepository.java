package com.example.repository;

import com.example.entity.WorkerLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkerLogRepository extends JpaRepository<WorkerLog, Long> {
    List<WorkerLog> findByEventTimestampBetweenAndWorkerName(LocalDateTime startTimestamp, LocalDateTime endTimestamp, String workerName);
    List<WorkerLog> findByShiftCode(String shiftCode);
}