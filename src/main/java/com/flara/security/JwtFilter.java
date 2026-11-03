package com.flara.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.JwtException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;

/**
 * Establishes the caller's identity from the {@code Authorization} header on
 * every inbound request.
 *
 * <p>Extends {@link OncePerRequestFilter}, which guarantees a single execution
 * per request even when the request is forwarded internally by the servlet
 * container.
 *
 * <p><b>This filter never rejects anyone.</b> It either populates the security
 * context or leaves it empty, then passes the request along either way.
 * Rejection happens further down the chain, where Spring's authorization filter
 * applies the {@code anyRequest().authenticated()} rule from
 * {@link SecurityConfig} and returns 401 if nothing was established here.
 * Keeping authentication and authorization as separate steps is deliberate.
 *
 * @see SecurityConfig
 * @see JwtUtil
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final FlaraUserDetailsService userDetailsService;

    /**
     * Reads the bearer token, if any, and authenticates the request against it.
     *
     * @param request     the inbound request
     * @param response    the outbound response
     * @param filterChain the remainder of the filter chain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // No credentials offered. This is normal for the public auth endpoints,
        // so hand the request on unauthenticated rather than failing here.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Strip the "Bearer " prefix to leave the raw token.
        final String token = authHeader.substring(7);

        try {
            // Parsing verifies the signature, so this throws on a tampered or
            // expired token rather than returning a value.
            final String username = jwtUtil.extractUsername(token);

            // The null check is defensive; the context check avoids re-authenticating
            // a request another filter has already established an identity for.
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(token, userDetails.getUsername())) {
                    // Credentials are passed as null: the token has already been
                    // verified, so there is no password to carry forward.
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
            // A bad token is a client problem, not a server fault. Clearing the
            // context and continuing lets the authorization filter answer with
            // 401; allowing the exception to escape would produce a 500 instead.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
