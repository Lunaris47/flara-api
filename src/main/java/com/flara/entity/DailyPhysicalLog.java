package com.flara.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Stores a user's daily physical symptom log.
 * Includes guided pain score, bowel tracking, and condition-specific symptoms.
 */
@Entity
@Table(name = "daily_physical_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyPhysicalLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate logDate;

    // ===============================
    // GUIDED PAIN SCORE (1-10)
    // Calculated from guided questions
    // ===============================
    private Integer painScore;

    // Raw answers from guided pain questions (stored as JSON string)
    @Column(columnDefinition = "TEXT")
    private String painAnswers;

    // ===============================
    // BOWEL TRACKING
    // ===============================
    private Integer bowelFrequency;

    // Bristol Stool Scale type (1-7)
    private Integer bristolType;

    // Blood presence: NONE, TRACE, MODERATE, SIGNIFICANT
    @Enumerated(EnumType.STRING)
    private BloodPresence bloodPresence;

    // ===============================
    // GENERAL SYMPTOMS
    // ===============================
    private Boolean fatigue;
    private Boolean jointPain;
    private Boolean nausea;
    private Boolean fever;
    private Boolean bloating;

    // ===============================
    // CROHN'S SPECIFIC SYMPTOMS
    // ===============================
    private Boolean perianalDiscomfort;
    private Boolean mouthSores;
    private Boolean skinIssues;

    // ===============================
    // UC SPECIFIC SYMPTOMS
    // ===============================
    private Boolean rectalBleeding;
    private Boolean urgency;
    private Boolean tenesmus;

    // ===============================
    // ENERGY & NOTES
    // ===============================

    // Energy level 1-10
    private Integer energyLevel;

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
    public enum BloodPresence {
        NONE, TRACE, MODERATE, SIGNIFICANT
    }
}