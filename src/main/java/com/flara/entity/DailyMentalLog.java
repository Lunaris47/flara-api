package com.flara.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * One day of mental state for a single user, mapped to the
 * {@code daily_mental_logs} table.
 *
 * <p>The counterpart to {@link DailyPhysicalLog}. Keeping the two apart is what
 * makes Flara's mind-gut correlation possible: a mental log and a physical log
 * share a {@code user} and a {@code logDate}, so stress on one day can be lined
 * up against symptoms on the same day or the days that follow, without either
 * entity having to carry the other's fields.
 *
 * <p>Lombok's {@code @Data} generates the accessors and {@code @Builder} the
 * fluent construction; the no-args constructor is required by JPA.
 */
@Entity
@Table(name = "daily_mental_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyMentalLog {

    /** Surrogate key, assigned by the database on insert. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The owning user.
     *
     * <p>Loaded lazily, and hidden from JSON output by {@code @JsonIgnore} so
     * that serializing a log cannot leak the user's stored password hash.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The calendar day this log describes. One log per user per day. */
    @Column(nullable = false)
    private LocalDate logDate;

    // ===============================
    // GUIDED STRESS SCORE (1-10)
    // Calculated from guided questions
    // ===============================

    /** Derived stress score on a 1 to 10 scale, computed from the guided answers. */
    private Integer stressScore;

    /**
     * Raw answers to the guided stress questions, serialized as a JSON string.
     * Kept as TEXT because the question set may change and the answers are only
     * ever read back whole.
     */
    @Column(columnDefinition = "TEXT")
    private String stressAnswers;

    // ===============================
    // MOOD & MENTAL HEALTH
    // ===============================

    /** Self-reported mood, 1 (worst) to 10 (best). */
    private Integer moodScore;

    /** Self-reported anxiety, 1 (lowest) to 10 (highest). */
    private Integer anxietyScore;

    // ===============================
    // SLEEP
    // ===============================

    /** Subjective sleep quality on a 1 to 10 scale. */
    private Integer sleepQuality;

    /** Hours slept. A {@code Double} so half hours can be recorded. */
    private Double sleepHours;

    // ===============================
    // STRESS EVENT
    // ===============================

    /**
     * The dominant source of stress for the day, if any.
     *
     * <p>Persisted as a string rather than an ordinal, so that adding or
     * reordering constants later cannot change what existing rows mean.
     */
    @Enumerated(EnumType.STRING)
    private StressEventType stressEventType;

    /** Whether the user completed a meditation or breathing exercise that day. */
    private Boolean meditationDone;

    /** Free-text notes. TEXT rather than varchar, as there is no useful limit. */
    @Column(columnDefinition = "TEXT")
    private String notes;

    // ===============================
    // TIMESTAMP
    // ===============================

    /**
     * When the row was first written, as distinct from {@code logDate}, which is
     * the day being described and may be backdated. Excluded from updates so the
     * original value survives later edits.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** JPA lifecycle hook: stamps {@code createdAt} immediately before the insert. */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ===============================
    // ENUMS
    // ===============================

    /**
     * Categories of stressor, used to group days when looking for patterns.
     * {@code NONE} is a deliberate answer rather than a missing one, which is why
     * it is a constant instead of a null.
     */
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
