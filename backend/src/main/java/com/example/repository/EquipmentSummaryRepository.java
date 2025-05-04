package com.example.repository;

import com.example.entity.EquipmentSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface EquipmentSummaryRepository extends JpaRepository<EquipmentSummary, Long> {
    List<EquipmentSummary> findBySummaryDateAndEquipmentSerial(Date summaryDate, String equipmentSerial);
}