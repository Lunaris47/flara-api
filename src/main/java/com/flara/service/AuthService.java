package com.flara.service;

import com.flara.dto.AuthResponse;
import com.flara.dto.LoginRequest;
import com.flara.dto.RegisterRequest;
import com.flara.entity.User;
import com.flara.repository.UserRepository;
import com.flara.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.flara.exception.ConflictException;
import com.flara.exception.NotFoundException;

/**
 * Registration and login.
 *
 * <p>The only service with no ownership checks, because at this point there is
 * nothing owned yet: these are the two operations that run before a caller has
 * an identity.
 *
 * <p>Both paths end the same way, by issuing a token. Registering therefore
 * logs the user straight in, with no separate login round trip.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * Creates an account and returns a token for it.
     *
     * <p>Uniqueness is checked in application code before the insert, even
     * though the database also enforces it. The database constraint is the real
     * guarantee; these checks exist so that a duplicate produces a clear 409
     * naming the field, rather than a constraint violation surfacing as a 500.
     *
     * <p>The password is hashed with BCrypt before it reaches the entity. The
     * plaintext is never stored and never logged.
     *
     * @param request the validated registration details
     * @return the new account's token and the fields the client needs on first render
     * @throws ConflictException if the username or email is already registered
     */
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already taken.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .condition(request.getCondition())
                .diagnosisDate(request.getDiagnosisDate())
                .knownTriggers(request.getKnownTriggers())
                .currentMedications(request.getCurrentMedications())
                .treatmentGoal(request.getTreatmentGoal())
                .darkMode(true)
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .condition(user.getCondition())
                .darkMode(user.getDarkMode())
                .build();
    }

    /**
     * Verifies credentials and returns a token.
     *
     * <p>The password is never compared here. Delegating to the
     * {@code AuthenticationManager} routes the check through the
     * {@code DaoAuthenticationProvider} configured in
     * {@code com.flara.security.SecurityConfig}, which loads the user via
     * {@code FlaraUserDetailsService} and performs the BCrypt comparison. A bad
     * password throws an {@code AuthenticationException} from inside that call,
     * which the global exception handler maps to 401.
     *
     * <p>The user is then loaded again to populate the response. That is a second
     * read of the same row, and could be avoided, but it keeps this method free
     * of assumptions about what the authentication provider left behind.
     *
     * @param request the submitted username and password
     * @return a fresh token and the fields the client needs on first render
     * @throws NotFoundException if the account vanished between authentication
     *                           and this lookup
     */
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found."));

        String token = jwtUtil.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .condition(user.getCondition())
                .darkMode(user.getDarkMode())
                .build();
    }
}
