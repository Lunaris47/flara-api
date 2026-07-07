package com.flara.repository;

import com.flara.entity.MealLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealLogRepository extends JpaRepository<MealLog, Long> {
    List<MealLog> findByUserIdOrderByLogDateDesc(Long userId);
    List<MealLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);
    List<MealLog> findByUserIdAndLogDateBetweenOrderByLogDateDesc(Long userId, LocalDate start, LocalDate end);
}