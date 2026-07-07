package com.flara.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Stores a user's daily mental health and stress log.
 * Core to Flara's mind-gut correlation feature.
 */
@Entity
@Table(name = "daily_mental_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyMentalLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate logDate;

    // ===============================
    // GUIDED STRESS SCORE (1-10)
    // Calculated from guided questions
    // ===============================
    private Integer stressScore;

    // Raw answers from guided stress questions (stored as JSON string)
    @Column(columnDefinition = "TEXT")
    private String stressAnswers;

    // ===============================
    // MOOD & MENTAL HEALTH
    // ===============================

    // Mood 1-10
    private Integer moodScore;

    // Anxiety 1-10
    private Integer anxietyScore;

    // ===============================
    // SLEEP
    // ===============================
    private Integer sleepQuality;
    private Double sleepHours;

    // ===============================
    // STRESS EVENT
    // ===============================

    @Enumerated(EnumType.STRING)
    private StressEventType stressEventType;

    private Boolean meditationDone;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // ===============================
    // TIMESTAMP
    // ===============================
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ===============================
    // ENUMS
    // ===============================
    public enum StressEventType {
        WORK,
        RELATIONSHIPS,
        MEDICAL_ANXIETY,
        FINANCIAL,
        FAMILY,
        OTHER,
        NONE
    }
}