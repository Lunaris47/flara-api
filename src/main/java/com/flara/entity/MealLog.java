package com.flara.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Stores a user's meal log entry.
 * Tracks food descriptions, IBD-specific trigger tags, and safety ratings.
 */
@Entity
@Table(name = "meal_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate logDate;

    private LocalDateTime mealTime;

    @Column(nullable = false)
    private String description;

    // ===============================
    // IBD TRIGGER TAGS (comma-separated)
    // e.g. "dairy,gluten,spicy"
    // ===============================
    private String triggerTags;

    // SAFE, NEUTRAL, TRIGGER
    @Enumerated(EnumType.STRING)
    private SafetyRating safetyRating;

    // Did a flare follow this meal?
    private Boolean precededFlare;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum SafetyRating {
        SAFE, NEUTRAL, TRIGGER
    }
}