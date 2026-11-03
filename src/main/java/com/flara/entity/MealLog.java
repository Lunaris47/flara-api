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
 * A single meal recorded by a user, mapped to the {@code meal_logs} table.
 *
 * <p>Unlike the daily logs, there are many of these per day. Each one carries
 * both what was eaten and the user's own assessment of it, which is what lets
 * the application build a picture of personal triggers over time rather than
 * relying on a generic list of foods to avoid.
 *
 * <p>The {@code precededFlare} flag is the link back to {@link Flare}: it is how
 * a meal becomes evidence rather than just a diary entry.
 */
@Entity
@Table(name = "meal_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealLog {

    /** Surrogate key, assigned by the database on insert. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The owning user. Loaded lazily, and excluded from JSON output so that
     * serializing a meal cannot expose the user's password hash.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The day the meal belongs to, used for grouping and date-range queries. */
    @Column(nullable = false)
    private LocalDate logDate;

    /**
     * The time the meal was eaten.
     *
     * <p>Separate from {@code logDate} and optional, because the ordering of
     * meals within a day matters when attributing a later flare, but users do
     * not always record it.
     */
    private LocalDateTime mealTime;

    /** What was eaten, in the user's own words. */
    @Column(nullable = false)
    private String description;

    // ===============================
    // IBD TRIGGER TAGS (comma-separated)
    // e.g. "dairy,gluten,spicy"
    // ===============================

    /**
     * Suspected trigger ingredients, stored as a comma-separated string.
     *
     * <p>Denormalized deliberately. A join table would allow querying by
     * individual tag, but the tag set is user-defined and free-form, and the
     * tags are only ever displayed as a group.
     */
    private String triggerTags;

    /**
     * The user's own classification of the meal.
     *
     * <p>Stored as a string rather than an ordinal so reordering the constants
     * cannot change what existing rows mean.
     */
    @Enumerated(EnumType.STRING)
    private SafetyRating safetyRating;

    /**
     * Whether a flare followed this meal.
     *
     * <p>Set by the user rather than inferred, since only they can judge whether
     * the timing was meaningful. This is the field that makes meal history useful
     * for spotting patterns.
     */
    private Boolean precededFlare;

    /** Free-text notes. TEXT rather than varchar, as there is no useful limit. */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * When the row was first written, as distinct from {@code logDate} or
     * {@code mealTime}. Excluded from updates so it survives later edits.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** JPA lifecycle hook: stamps {@code createdAt} immediately before the insert. */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /** How the user rated this meal for themselves, not a clinical judgement. */
    public enum SafetyRating {
        SAFE, NEUTRAL, TRIGGER
    }
}
