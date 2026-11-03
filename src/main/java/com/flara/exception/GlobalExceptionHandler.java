package com.flara.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Translates exceptions into HTTP responses for the entire API.
 *
 * <p>The principle this enforces: <b>services throw meaning, this class decides
 * the status code.</b> No service in the codebase returns a
 * {@link ResponseEntity} or mentions an HTTP status. They describe what went
 * wrong in domain terms and the translation happens here, once.
 *
 * <p>{@code @RestControllerAdvice} registers these handlers across every
 * controller, and serializes whatever they return as JSON. An exception
 * propagates out of a controller method, Spring's dispatcher looks for a
 * handler matching its type, and the matching method below produces the
 * response.
 *
 * <p>Because every response is assembled by {@link #build}, each error in the
 * API has an identical shape, so a client writes one error handler rather than
 * guessing at the form of each failure.
 *
 * <p><b>Gap worth knowing about:</b> there is no catch-all
 * {@code @ExceptionHandler(Exception.class)}. Anything not typed below still
 * produces Spring's default 500. A production system would add one that logs
 * the real exception and returns a generic message, so that stack traces, SQL
 * fragments and class names never reach a client.
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * A record that does not exist: 404.
     *
     * @param ex the thrown exception, whose message reaches the client
     * @return a 404 response
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * A record that exists but belongs to someone else: 403.
     *
     * <p>Every exception reaching this handler originates from an ownership
     * check in the service layer. This is the handler that makes those checks
     * visible to the client as a refusal rather than as a server fault.
     *
     * @param ex the thrown exception, whose message reaches the client
     * @return a 403 response
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    /**
     * A request clashing with existing data, such as a duplicate username: 409.
     *
     * @param ex the thrown exception, whose message reaches the client
     * @return a 409 response
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Failed authentication: 401.
     *
     * <p>Covers a wrong password and, because {@code UsernameNotFoundException}
     * extends {@link AuthenticationException}, a token naming an account that no
     * longer exists.
     *
     * <p>The message is deliberately generic rather than taken from the
     * exception. Saying whether the username or the password was wrong tells an
     * attacker which usernames are registered.
     *
     * @param ex the thrown exception, whose message is intentionally discarded
     * @return a 401 response
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
    }

    /**
     * A request body failing its {@code @Valid} constraints: 400.
     *
     * <p>Spring raises this before the controller body runs. Only the first
     * field error is reported, which keeps the message short and gives the
     * client something specific to show, such as
     * {@code "email must be a well-formed email address"}.
     *
     * @param ex the validation failure, carrying every field error
     * @return a 400 response naming the first problem found
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request.");
        return build(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Assembles the error body shared by every handler above.
     *
     * <p>A {@link LinkedHashMap} rather than a {@code HashMap} so the keys
     * appear in insertion order; a scrambled error body is harder to read in a
     * log or a network tab.
     *
     * <p>Could be {@code private}: nothing outside this class calls it.
     *
     * @param status  the status to return
     * @param message the detail shown to the client
     * @return the response, ready to serialize
     */
    public ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
