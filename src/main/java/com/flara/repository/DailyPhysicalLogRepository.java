package com.flara.repository;

import com.flara.entity.DailyPhysicalLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyPhysicalLogRepository extends JpaRepository<DailyPhysicalLog, Long> {
    List<DailyPhysicalLog> findByUserIdOrderByLogDateDesc(Long userId);
    Optional<DailyPhysicalLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);
    List<DailyPhysicalLog> findByUserIdAndLogDateBetweenOrderByLogDateDesc(Long userId, LocalDate start, LocalDate end);
}