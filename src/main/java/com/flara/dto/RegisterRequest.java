package com.flara.dto;

import com.flara.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request body for user registration.
 */
@Data
public class RegisterRequest {

    @NotBlank
    private String username;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotNull
    private User.Condition condition;

    private LocalDate diagnosisDate;
    private String knownTriggers;
    private String currentMedications;
    private String treatmentGoal;
}