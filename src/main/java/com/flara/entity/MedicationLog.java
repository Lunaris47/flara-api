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
 * A single medication dose recorded by a user, mapped to the
 * {@code medication_logs} table.
 *
 * <p>Each row represents one scheduled dose rather than one prescription, which
 * is what allows adherence to be tracked: a missed dose is a row with
 * {@code taken} set to false, not an absent row. That distinction matters for a
 * doctor's report, where "did not take it" and "never recorded it" mean
 * different things.
 *
 * <p>Biologics are handled slightly differently from oral medication, since they
 * are administered on an infusion schedule rather than daily. See
 * {@code biologicDate}.
 */
@Entity
@Table(name = "medication_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationLog {

    /** Surrogate key, assigned by the database on insert. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The owning user. Loaded lazily, and excluded from JSON output so that
     * serializing a dose cannot expose the user's password hash.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The day this dose was scheduled for. */
    @Column(nullable = false)
    private LocalDate logDate;

    /**
     * Medication name as the user entered it.
     *
     * <p>Free text rather than a reference to a drug catalogue, which keeps the
     * application usable without integrating a medication database.
     */
    @Column(nullable = false)
    private String medicationName;

    /** Dose as written on the prescription, for example "40mg". */
    private String dosage;

    /**
     * Drug class, persisted as a string so reordering the constants cannot
     * change the meaning of existing rows.
     */
    @Enumerated(EnumType.STRING)
    private MedicationType medicationType;

    /**
     * Whether the dose was actually taken.
     *
     * <p>A false value is meaningful data, not a missing value: it is how
     * non-adherence is recorded and reported.
     */
    private Boolean taken;

    /**
     * When the dose was taken.
     *
     * <p>Separate from {@code logDate} because timing within the day matters for
     * medications that must be spaced or taken with food.
     */
    private LocalDateTime takenAt;

    /**
     * Infusion or injection date, for biologics only.
     *
     * <p>Biologics are given every few weeks rather than daily, so the relevant
     * date is the administration date rather than the daily log date. Null for
     * every other medication type.
     */
    private LocalDate biologicDate;

    /**
     * Any side effects the user noticed. TEXT rather than varchar, since this is
     * the field most likely to be written at length and most useful to a doctor.
     */
    @Column(columnDefinition = "TEXT")
    private String sideEffectNotes;

    /**
     * When the row was first written, as distinct from the dates above.
     * Excluded from updates so it survives later edits.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** JPA lifecycle hook: stamps {@code createdAt} immediately before the insert. */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /** Drug classes relevant to inflammatory bowel disease treatment. */
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
