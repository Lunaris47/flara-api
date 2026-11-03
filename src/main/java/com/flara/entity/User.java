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
 * A registered Flara account, mapped to the {@code users} table.
 *
 * <p>Owns every log in the system. All five log entities hold a
 * {@code @ManyToOne} reference back to this class, and that reference is what
 * the service layer compares against the authenticated caller to decide whether
 * a record may be read or modified.
 *
 * <p>This entity is never returned directly from an endpoint. The
 * {@code password} field holds a BCrypt hash, and the log entities mark their
 * {@code user} reference {@code @JsonIgnore} precisely so it cannot leak through
 * a nested serialization. Authentication responses use
 * {@code com.flara.dto.AuthResponse} instead.
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /** Surrogate key, assigned by the database on insert. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Login name. Unique at the database level, and also checked in
     * {@code AuthService} before insert so a duplicate produces a clean 409
     * rather than a constraint violation.
     */
    @NotBlank
    @Column(unique = true, nullable = false)
    private String username;

    /** Contact address. Unique, and validated for shape on registration. */
    @Email
    @NotBlank
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * BCrypt hash of the password. Never the plaintext.
     *
     * <p>No salt column exists because BCrypt generates a random salt per
     * password and stores it inside the hash itself.
     */
    @NotBlank
    @Column(nullable = false)
    private String password;

    // ===============================
    // CONDITION INFO
    // ===============================

    /**
     * Which form of inflammatory bowel disease the user has.
     *
     * <p>Drives which symptom fields the client surfaces on a daily physical
     * log, since Crohn's and ulcerative colitis present differently. Persisted
     * as a string rather than an ordinal so reordering the constants cannot
     * change what existing rows mean.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Condition condition;

    /** Date of diagnosis. Optional, since not every user knows or wants to say. */
    private LocalDate diagnosisDate;

    // ===============================
    // ONBOARDING DATA
    // Captured at sign-up to give the
    // app something to work with on day one
    // ===============================

    /** Known food triggers, comma-separated, as entered during onboarding. */
    private String knownTriggers;

    /** Medications the user is already taking, comma-separated. */
    private String currentMedications;

    /** What the user is hoping to get out of tracking, in their own words. */
    private String treatmentGoal;

    // ===============================
    // PREFERENCES
    // ===============================

    /**
     * Client display preference, defaulted to dark mode.
     *
     * <p>{@code @Builder.Default} is required here: without it, Lombok's builder
     * would ignore the field initializer and leave this null, violating the
     * {@code nullable = false} constraint.
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean darkMode = true;

    // ===============================
    // TIMESTAMPS
    // ===============================

    /** Account creation time. Written once and excluded from every update. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** JPA lifecycle hook: stamps {@code createdAt} immediately before the insert. */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ===============================
    // CONDITION ENUM
    // ===============================

    /** The two forms of inflammatory bowel disease the application supports. */
    public enum Condition {
        CROHNS,
        UC
    }
}
