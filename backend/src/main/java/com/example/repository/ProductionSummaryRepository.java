package com.example.repository;

import com.example.entity.ProductionSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface ProductionSummaryRepository extends JpaRepository<ProductionSummary, Long> {
    List<ProductionSummary> findBySummaryDateAndFactoryName(Date summaryDate, String factoryName);
    List<ProductionSummary> findByWorkOrderName(String workOrderName);
}