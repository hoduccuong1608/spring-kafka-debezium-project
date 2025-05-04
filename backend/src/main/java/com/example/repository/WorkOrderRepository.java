package com.example.repository;

import com.example.entity.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    WorkOrder findByWorkOrderName(String workOrderName);
}
