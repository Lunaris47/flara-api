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
 * One day of physical symptoms for a single user, mapped to the
 * {@code daily_physical_logs} table.
 *
 * <p>Physical and mental state are stored as two separate entities
 * ({@link DailyMentalLog} is the counterpart) rather than one wide row. They are
 * recorded on different cadences and share no fields, so combining them would
 * produce a table half full of nulls on every row. The two are correlated by
 * {@code logDate} and {@code user} when a report or dashboard needs both.
 *
 * <p>Symptom fields are grouped below by whether they apply to all users or only
 * to one condition, since a Crohn's user and a UC user answer different
 * questions.
 *
 * <p>Lombok's {@code @Data} generates the getters, setters, {@code equals},
 * {@code hashCode} and {@code toString}; {@code @Builder} provides the fluent
 * construction used by the service layer. The no-args and all-args constructors
 * are required by JPA and by the builder respectively.
 */
@Entity
@Table(name = "daily_physical_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyPhysicalLog {

    /** Surrogate key, assigned by the database on insert. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The owning user.
     *
     * <p>{@code LAZY} prevents Hibernate loading the full user row unless
     * something actually reads it. {@code @JsonIgnore} is load-bearing for
     * security: without it, serializing this log to JSON would walk into the
     * user object and expose the stored password hash to the client.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The calendar day this log describes. One log per user per day. */
    @Column(nullable = false)
    private LocalDate logDate;

    // ===============================
    // GUIDED PAIN SCORE (1-10)
    // Calculated from guided questions
    // ===============================

    /** Derived pain score on a 1 to 10 scale, computed from the guided answers. */
    private Integer painScore;

    /**
     * Raw answers to the guided pain questions, serialized as a JSON string.
     *
     * <p>Stored as TEXT rather than modelled as its own table because the
     * question set is expected to change over time and the answers are only ever
     * read back as a whole, never queried field by field.
     */
    @Column(columnDefinition = "TEXT")
    private String painAnswers;

    // ===============================
    // BOWEL TRACKING
    // ===============================

    /** Number of bowel movements recorded for the day. */
    private Integer bowelFrequency;

    /** Bristol Stool Scale classification, 1 (hard) through 7 (liquid). */
    private Integer bristolType;

    /**
     * Per-movement detail for the day, serialized as a JSON array.
     *
     * <p>Example: {@code [{"type":4,"blood":"NONE"},{"type":6,"blood":"TRACE"}]}.
     * Kept alongside the daily aggregates above so a clinician can see the
     * distribution within a day, not only the totals.
     */
    @Column(columnDefinition = "TEXT")
    private String bowelMovementLogs;

    /**
     * Worst blood presence observed during the day.
     *
     * <p>Persisted as a string rather than an ordinal so that reordering the
     * enum constants cannot silently change the meaning of existing rows.
     */
    @Enumerated(EnumType.STRING)
    private BloodPresence bloodPresence;

    // ===============================
    // GENERAL SYMPTOMS
    // Apply regardless of diagnosis
    // ===============================
    private Boolean fatigue;
    private Boolean jointPain;
    private Boolean nausea;
    private Boolean fever;
    private Boolean bloating;

    // ===============================
    // CROHN'S SPECIFIC SYMPTOMS
    // Surfaced only to users with CROHNS
    // ===============================
    private Boolean perianalDiscomfort;
    private Boolean mouthSores;
    private Boolean skinIssues;

    // ===============================
    // UC SPECIFIC SYMPTOMS
    // Surfaced only to users with UC
    // ===============================
    private Boolean rectalBleeding;
    private Boolean urgency;
    private Boolean tenesmus;

    // ===============================
    // ENERGY & NOTES
    // ===============================

    /** Self-reported energy on a 1 to 10 scale. */
    private Integer energyLevel;

    /** Free-text notes. TEXT rather than varchar, as there is no useful limit. */
    @Column(columnDefinition = "TEXT")
    private String notes;

    // ===============================
    // TIMESTAMP
    // ===============================

    /**
     * When the row was first written. Distinct from {@code logDate}, which is the
     * day being described and may be backdated by the user.
     *
     * <p>{@code updatable = false} means Hibernate excludes this column from every
     * UPDATE, so the original value survives edits to the rest of the row.
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

    /** Severity of blood observed, from none through to significant. */
    public enum BloodPresence {
        NONE, TRACE, MODERATE, SIGNIFICANT
    }
}
