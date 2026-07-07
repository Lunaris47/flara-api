package com.flara.repository;

import com.flara.entity.MedicationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicationLogRepository extends JpaRepository<MedicationLog, Long> {
    List<MedicationLog> findByUserIdOrderByLogDateDesc(Long userId);
    List<MedicationLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);
    List<MedicationLog> findByUserIdAndLogDateBetweenOrderByLogDateDesc(Long userId, LocalDate start, LocalDate end);
}