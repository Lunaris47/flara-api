package com.flara.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A single flare episode, mapped to the {@code flares} table.
 *
 * <p>Where the daily logs capture a user's baseline, a flare captures an acute
 * event. It is bounded by a start and an optional end, and it records what was
 * happening both physically and mentally beforehand, which is what makes it
 * useful for identifying triggers rather than simply counting episodes.
 *
 * <p>The end date is nullable on purpose: a flare that is still in progress has
 * a start and no end, and that is a valid state rather than incomplete data.
 */
@Entity
@Table(name = "flares")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Flare {

    /** Surrogate key, assigned by the database on insert. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The owning user.
     *
     * <p>Loaded lazily, and hidden from JSON by {@code @JsonIgnore} so that
     * serializing a flare cannot walk into the user and expose the stored
     * password hash. This field is also what the service layer compares against
     * the caller to enforce ownership.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * When the flare began. Defaulted to the current time by the service layer
     * if the client does not supply one.
     */
    @Column(nullable = false)
    private LocalDateTime startDate;

    /** When the flare resolved. Null while the flare is still ongoing. */
    private LocalDateTime endDate;

    /** Self-reported severity on a 1 to 10 scale. */
    private Integer severity;

    /**
     * What was happening physically in the lead-up, in the user's own words.
     * Paired with {@code mentalContext} so both sides are captured at the moment
     * of recording, while the detail is still fresh.
     */
    @Column(columnDefinition = "TEXT")
    private String physicalContext;

    /** What was happening mentally or emotionally in the lead-up. */
    @Column(columnDefinition = "TEXT")
    private String mentalContext;

    /** Anything the user suspects set this flare off. */
    @Column(columnDefinition = "TEXT")
    private String potentialTriggers;

    /** Whether the flare settled without intervention. */
    private Boolean resolvedNaturally;

    /**
     * Whether the flare required medical attention. Distinct from
     * {@code severity}, since a moderate flare can still send someone to a
     * clinic, and this is the field a doctor is most likely to scan for.
     */
    private Boolean requiredMedicalAttention;

    /** Free-text notes. TEXT rather than varchar, as there is no useful limit. */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * When the row was first written, as distinct from {@code startDate}, which
     * may be backdated. Excluded from updates so the original value survives
     * later edits to the flare.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** JPA lifecycle hook: stamps {@code createdAt} immediately before the insert. */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
