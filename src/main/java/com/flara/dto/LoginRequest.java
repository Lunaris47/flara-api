package com.flara.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for user login.
 */
@Data
public class LoginRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}