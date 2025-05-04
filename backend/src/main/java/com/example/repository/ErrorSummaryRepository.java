package com.example.repository;

import com.example.entity.ErrorSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface ErrorSummaryRepository extends JpaRepository<ErrorSummary, Long> {
    List<ErrorSummary> findBySummaryDateAndErrorCode(Date summaryDate, String errorCode);
    List<ErrorSummary> findByWorkerName(String workerName);
}