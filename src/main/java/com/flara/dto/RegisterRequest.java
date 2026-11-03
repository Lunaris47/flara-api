package com.flara.dto;

import com.flara.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * Details submitted when creating an account.
 *
 * <p>The reason this exists rather than binding the {@link User} entity
 * directly: registration must never accept an {@code id}, a {@code createdAt}
 * or a pre-hashed password from the client. Listing the permitted fields
 * explicitly makes that impossible rather than merely unlikely.
 *
 * <p>Validation runs before {@code AuthService} is called, because
 * {@code AuthController} marks the parameter {@code @Valid}. A failure is
 * reported as a 400 naming the field that failed.
 */
@Data
public class RegisterRequest {

    /** Desired login name. Checked for uniqueness by the service. */
    @NotBlank
    private String username;

    /** Contact address. Checked for shape here and for uniqueness by the service. */
    @Email
    @NotBlank
    private String email;

    /** Plaintext password. Hashed with BCrypt before it reaches the entity. */
    @NotBlank
    private String password;

    /** Crohn's or UC. Required, since it drives which symptoms the client shows. */
    @NotNull
    private User.Condition condition;

    // Optional onboarding detail. Absent values are acceptable: these give the
    // application something to work with on day one, but none is required to
    // create an account.

    /** Date of diagnosis, if the user knows or wishes to share it. */
    private LocalDate diagnosisDate;

    /** Known food triggers, comma separated. */
    private String knownTriggers;

    /** Medications already being taken, comma separated. */
    private String currentMedications;

    /** What the user hopes to get out of tracking, in their own words. */
    private String treatmentGoal;
}