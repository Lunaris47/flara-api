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
 * Represents a recorded flare episode.
 * Tracks start/end dates, severity, and contextual notes
 * for use in the mind-gut correlation dashboard and doctor reports.
 */
@Entity
@Table(name = "flares")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Flare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    // Severity 1-10
    private Integer severity;

    // What was happening physically before the flare
    @Column(columnDefinition = "TEXT")
    private String physicalContext;

    // What was happening mentally/emotionally before the flare
    @Column(columnDefinition = "TEXT")
    private String mentalContext;

    // Potential triggers identified
    @Column(columnDefinition = "TEXT")
    private String potentialTriggers;

    private Boolean resolvedNaturally;
    private Boolean requiredMedicalAttention;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}