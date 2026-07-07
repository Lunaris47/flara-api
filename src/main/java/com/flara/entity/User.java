package com.flara.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a registered Flara user.
 * Stores account info, IBD condition type, and onboarding data.
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String username;

    @Email
    @NotBlank
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    // ===============================
    // CONDITION INFO
    // ===============================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Condition condition;

    // Date the user was diagnosed
    private LocalDate diagnosisDate;

    // ===============================
    // ONBOARDING DATA
    // ===============================

    // Known food triggers (comma-separated)
    private String knownTriggers;

    // Current medications (comma-separated)
    private String currentMedications;

    // User's primary treatment goal
    private String treatmentGoal;

    // ===============================
    // PREFERENCES
    // ===============================

    @Builder.Default
    @Column(nullable = false)
    private Boolean darkMode = true;

    // ===============================
    // TIMESTAMPS
    // ===============================

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ===============================
    // CONDITION ENUM
    // ===============================

    public enum Condition {
        CROHNS,
        UC
    }
}