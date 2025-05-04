package com.example.repository;

import com.example.entity.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {
    List<ErrorLog> findByEventTimestampBetweenAndErrorCode(LocalDateTime startTimestamp, LocalDateTime endTimestamp, String errorCode);
    List<ErrorLog> findByWorkerName(String workerName);
}