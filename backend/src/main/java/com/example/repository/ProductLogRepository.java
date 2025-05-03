package com.example.repository;

import com.example.entity.ProductLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductLogRepository extends JpaRepository<ProductLog, Long> {
}
