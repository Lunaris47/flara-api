package com.flara.exception;

/**
 * Thrown when a request clashes with data that already exists.
 *
 * <p>Used during registration for a duplicate username or email. The database
 * also enforces uniqueness; this exception exists so the clash is reported as a
 * clear 409 naming the field, rather than surfacing as a constraint violation.
 *
 * <p>Mapped to 409 Conflict by {@link GlobalExceptionHandler}. Unchecked by design:
 * a checked exception would force {@code throws} declarations onto every method
 * between the throw site and the controller, when the only code that handles it
 * is the global handler.
 */
public class ConflictException extends RuntimeException {

    /**
     * @param message detail shown to the client in the error response body
     */
    public ConflictException(String message) {
        super(message);
    }
}
