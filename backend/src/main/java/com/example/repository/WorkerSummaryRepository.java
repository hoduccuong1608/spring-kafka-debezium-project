package com.example.repository;

import com.example.entity.WorkerSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface WorkerSummaryRepository extends JpaRepository<WorkerSummary, Long> {
    List<WorkerSummary> findBySummaryDateAndWorkerName(Date summaryDate, String workerName);
    List<WorkerSummary> findByShiftCode(String shiftCode);
}