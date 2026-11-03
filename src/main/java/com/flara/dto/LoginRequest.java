package com.flara.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Credentials submitted to the login endpoint.
 *
 * <p>Deliberately narrow: login accepts a username and a password and nothing
 * else. Binding the {@link com.flara.entity.User} entity here would let a client
 * send fields it has no business setting.
 *
 * <p>The constraints run because {@code AuthController} marks the parameter
 * {@code @Valid}; a violation is converted to a 400 naming the offending field.
 */
@Data
public class LoginRequest {

    /** Login name. Must be present and non-blank. */
    @NotBlank
    private String username;

    /** Plaintext password, compared against the stored BCrypt hash. */
    @NotBlank
    private String password;
}