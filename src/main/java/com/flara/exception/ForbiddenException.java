package com.flara.exception;

/**
 * Thrown when an authenticated caller asks for a record they do not own.
 *
 * <p>Distinct from a 401, which means the API does not know who the caller is.
 * A 403 means it knows exactly who they are and the answer is still no. Every
 * instance of this exception comes from an ownership check in the service
 * layer; Spring Security never raises it.
 *
 * <p>Mapped to 403 Forbidden by {@link GlobalExceptionHandler}. Unchecked by design:
 * a checked exception would force {@code throws} declarations onto every method
 * between the throw site and the controller, when the only code that handles it
 * is the global handler.
 */
public class ForbiddenException extends RuntimeException {

    /**
     * @param message detail shown to the client in the error response body
     */
    public ForbiddenException(String message) {
        super(message);
    }
}
