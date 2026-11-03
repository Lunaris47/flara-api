package com.flara.dto;

import com.flara.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * What the API returns after a successful registration or login.
 *
 * <p>Exists so that the {@link User} entity is never serialized to a client.
 * The entity carries the BCrypt password hash and every onboarding field; this
 * carries the token plus the handful of values the front end needs in order to
 * render its first screen without a second request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /** Signed JWT the client attaches to subsequent requests. */
    private String token;

    /** The authenticated user's login name. */
    private String username;

    /** The authenticated user's email address. */
    private String email;

    /** Crohn's or UC, which determines the symptom set the client shows. */
    private User.Condition condition;

    /** Display preference, returned so the client can theme itself immediately. */
    private Boolean darkMode;
}