package com.flara.controller;

import com.flara.dto.AuthResponse;
import com.flara.dto.LoginRequest;
import com.flara.dto.RegisterRequest;
import com.flara.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * The two public endpoints: registration and login.
 *
 * <p>The only controller reachable without a token, because
 * {@code SecurityConfig} leaves {@code /api/auth/**} open. Everything else in
 * the API requires an authenticated caller, and a caller cannot become
 * authenticated without one of these two calls succeeding first.
 *
 * <p>Also the only controller that takes and returns DTOs rather than entities,
 * which is the pattern the rest of the API should follow.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Creates an account and returns a token for it, so registering also logs
     * the user in.
     *
     * <p>{@code @Valid} runs the constraints on {@link RegisterRequest} before
     * the service is called, so malformed input never reaches the business
     * logic.
     *
     * @param request the registration details
     * @return the new account's token and initial display fields
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Verifies credentials and issues a token.
     *
     * <p>Bad credentials produce an {@code AuthenticationException} inside the
     * service, which the global handler converts to 401.
     *
     * @param request the submitted username and password
     * @return a fresh token and the user's initial display fields
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}