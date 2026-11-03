package com.flara.exception;

/**
 * Thrown when a record does not exist.
 *
 * <p>Means the identifier is unknown, not that the caller lacks permission.
 * Where a record exists but belongs to someone else, {@link ForbiddenException}
 * is thrown instead, so the two cases stay distinguishable in the response.
 *
 * <p>Mapped to 404 Not Found by {@link GlobalExceptionHandler}. Unchecked by design:
 * a checked exception would force {@code throws} declarations onto every method
 * between the throw site and the controller, when the only code that handles it
 * is the global handler.
 */
public class NotFoundException extends RuntimeException {

    /**
     * @param message detail shown to the client in the error response body
     */
    public NotFoundException(String message) {
        super(message);
    }
}
