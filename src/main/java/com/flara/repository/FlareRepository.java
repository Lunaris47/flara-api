package com.flara.repository;

import com.flara.entity.Flare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlareRepository extends JpaRepository<Flare, Long> {
    List<Flare> findByUserIdOrderByStartDateDesc(Long userId);
    List<Flare> findByUserIdAndStartDateBetweenOrderByStartDateDesc(Long userId, LocalDateTime start, LocalDateTime end);
}