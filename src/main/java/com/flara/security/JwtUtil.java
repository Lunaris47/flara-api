package com.flara.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HexFormat;

/**
 * Creates and validates the JSON Web Tokens used to authenticate API requests.
 *
 * <p>This class is deliberately free of Spring Security types. It understands
 * tokens and nothing else, which keeps the boundary clean: {@link JwtFilter}
 * handles the HTTP side, {@link FlaraUserDetailsService} handles the user
 * lookup, and this class handles the cryptography.
 *
 * <p>Tokens are signed with HMAC-SHA using a secret supplied at startup. Signing
 * proves the token was issued by this server; it does <em>not</em> encrypt the
 * payload, so a token's claims are readable by anyone holding it and must never
 * contain sensitive data.
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expiration;

    /**
     * Derives the signing key from configuration.
     *
     * <p>The secret is supplied as a hexadecimal string and decoded to raw bytes,
     * so a 64-character property yields the 32 bytes that HMAC-SHA256 requires.
     * A shorter secret causes {@code Keys.hmacShaKeyFor} to reject the key and the
     * application to fail at startup rather than at the first login.
     *
     * @param secret     hex-encoded signing secret, from {@code jwt.secret}
     * @param expiration token lifetime in milliseconds, from {@code jwt.expiration}
     */
    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {
        byte[] keyBytes = HexFormat.of().parseHex(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;
    }

    /**
     * Issues a signed token identifying the given user.
     *
     * <p>The username is stored as the token's subject. Issue and expiry
     * timestamps are stamped at creation, so the lifetime is fixed the moment the
     * token is handed out and cannot be extended afterwards.
     *
     * @param username the user the token represents
     * @return a signed, compact JWT string
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Reads the username from a token.
     *
     * <p>Parsing verifies the signature as part of the same operation, so this
     * cannot return a value from a tampered token: it throws instead. Callers must
     * be prepared for that, which is why {@link JwtFilter} wraps the call.
     *
     * @param token the raw token, without the {@code Bearer } prefix
     * @return the username stored as the token's subject
     * @throws io.jsonwebtoken.JwtException if the token is malformed, expired or
     *                                      not signed by this server
     */
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Confirms that a token belongs to the expected user and has not expired.
     *
     * <p>The username comparison guards against a valid, correctly signed token
     * being accepted for a different account than the one being loaded.
     *
     * @param token    the raw token
     * @param username the username the caller expects the token to identify
     * @return {@code true} if the token is current and matches that user
     */
    public boolean validateToken(String token, String username) {
        return extractUsername(token).equals(username) && !isTokenExpired(token);
    }

    /**
     * Parses a token and returns its claims, verifying the signature in the
     * process. This is the single point at which token trust is established;
     * every public method above funnels through it.
     *
     * @param token the raw token
     * @return the verified claims
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * @param token the raw token
     * @return {@code true} if the token's expiry timestamp is in the past
     */
    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }
}
