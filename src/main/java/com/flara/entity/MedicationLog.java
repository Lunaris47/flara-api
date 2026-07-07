package com.flara.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Stores a user's medication and treatment log.
 * Tracks dosage, timing, adherence, and biologic infusion dates.
 */
@Entity
@Table(name = "medication_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate logDate;

    @Column(nullable = false)
    private String medicationName;

    private String dosage;

    @Enumerated(EnumType.STRING)
    private MedicationType medicationType;

    private Boolean taken;

    private LocalDateTime takenAt;

    // For biologics — infusion or injection date
    private LocalDate biologicDate;

    @Column(columnDefinition = "TEXT")
    private String sideEffectNotes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum MedicationType {
        BIOLOGIC,
        IMMUNOSUPPRESSANT,
        STEROID,
        AMINOSALICYLATE,
        ANTIBIOTIC,
        SUPPLEMENT,
        OTHER
    }
}