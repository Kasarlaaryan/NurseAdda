package com.nurseadda.project.repository;

import com.nurseadda.project.entity.RateConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RateConfigRepository extends JpaRepository<RateConfig, Long> {
    Optional<RateConfig> findByShiftType(String shiftType);
}
