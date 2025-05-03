package com.example.repository;

import com.example.entity.ErrorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorTypeRepository extends JpaRepository<ErrorType, Long> {
}
