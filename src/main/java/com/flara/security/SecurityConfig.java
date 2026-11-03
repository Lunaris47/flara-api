package com.flara.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Central security configuration for the Flara API.
 *
 * <p>Defines a stateless, token-based security model: there is no server-side
 * session, and every request is authenticated independently from the JSON Web
 * Token it carries. The beans declared here are assembled by Spring at startup
 * and together form the rulebook applied to every inbound request.
 *
 * <p>Two responsibilities are deliberately kept apart:
 * <ul>
 *   <li><b>Authentication</b> (who is calling) is established by {@link JwtFilter}
 *       and, for the login endpoint, by the {@link DaoAuthenticationProvider}
 *       declared below.</li>
 *   <li><b>Authorization</b> (what they may touch) is split in two: coarse URL
 *       rules live in {@link #securityFilterChain(HttpSecurity)}, while per-record
 *       ownership checks live in the service layer.</li>
 * </ul>
 *
 * @see JwtFilter
 * @see FlaraUserDetailsService
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final FlaraUserDetailsService userDetailsService;

    /**
     * Comma-separated list of browser origins permitted to call this API,
     * supplied by the {@code cors.allowed-origins} property.
     *
     * <p>Declared as a non-final field rather than a constructor parameter because
     * Lombok's {@code @RequiredArgsConstructor} only includes final fields and does
     * not copy {@code @Value} onto the generated parameter, so a final field here
     * would never be populated.
     */
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * Builds the filter chain every request passes through.
     *
     * <p>The configuration, in order:
     * <ul>
     *   <li><b>CORS</b> is applied from {@link #corsConfigurationSource()}.</li>
     *   <li><b>CSRF is disabled</b>, which follows from the stateless design rather
     *       than being a shortcut. CSRF attacks rely on the browser attaching a
     *       session cookie automatically; a bearer token the client must attach by
     *       hand gives an attacker nothing to forge.</li>
     *   <li><b>URL rules</b> leave {@code /api/auth/**} public, since registration
     *       and login must be reachable before a token exists, and require
     *       authentication everywhere else.</li>
     *   <li><b>Sessions are disabled</b> entirely. Nothing is remembered between
     *       requests.</li>
     *   <li>{@link JwtFilter} is inserted <em>before</em> Spring's form-login filter
     *       so the security context is already populated by the time authorization
     *       is evaluated.</li>
     * </ul>
     *
     * @param http the builder Spring supplies for assembling the chain
     * @return the configured filter chain
     * @throws Exception if the chain cannot be built
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Defines which cross-origin browser requests are accepted.
     *
     * <p>Note that CORS is enforced by the browser, not by this server, so these
     * rules affect browser-based clients only. A mobile application or a
     * server-to-server caller sends no {@code Origin} header and is unaffected.
     *
     * <p>{@code Authorization} appears in both the allowed and the exposed header
     * lists: allowed so the client may send the bearer token, exposed so the
     * browser permits client-side code to read it back off a response.
     *
     * @return the CORS policy, applied to every path
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Split the configured property into individual origins. Trimming allows
        // the environment variable to be written with spaces after the commas.
        configuration.setAllowedOriginPatterns(
                Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        // Browsers may cache the preflight response for an hour, which avoids an
        // OPTIONS round trip before every cross-origin call.
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * The component that verifies a username and password pair at login.
     *
     * <p>Pairs {@link FlaraUserDetailsService}, which loads the stored user, with
     * the BCrypt encoder, which compares the submitted password against the stored
     * hash. Used by {@link #authenticationManager(AuthenticationConfiguration)} and
     * therefore by {@code AuthService.login}.
     *
     * @return the configured authentication provider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes Spring's {@code AuthenticationManager} as a bean so it can be
     * injected into {@code AuthService}.
     *
     * <p>The checked exception is wrapped because this runs during application
     * startup: a failure here means the context cannot be built, so there is no
     * meaningful recovery and failing fast is the correct behaviour.
     *
     * @param config Spring's authentication configuration
     * @return the application's authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        try {
            return config.getAuthenticationManager();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The password hashing strategy for the whole application.
     *
     * <p>BCrypt generates a random salt per password and stores it inside the
     * resulting hash, which is why no separate salt column exists on the user
     * table. It is also deliberately slow, which is the property that makes
     * brute-force attacks expensive.
     *
     * @return the password encoder used on registration and on every login
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
