package com.flara.repository;

import com.flara.entity.DailyMentalLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyMentalLogRepository extends JpaRepository<DailyMentalLog, Long> {
    List<DailyMentalLog> findByUserIdOrderByLogDateDesc(Long userId);
    Optional<DailyMentalLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);
    List<DailyMentalLog> findByUserIdAndLogDateBetweenOrderByLogDateDesc(Long userId, LocalDate start, LocalDate end);
}