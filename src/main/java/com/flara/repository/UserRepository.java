package com.flara.repository;

import com.flara.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data repository for user accounts.
 *
 * <p>No implementation is written for this interface. Spring Data generates one
 * at startup, supplying the standard CRUD operations from {@link JpaRepository}
 * and deriving a query for each method below by parsing its name.
 *
 * <p>This is the only repository not scoped by user id, because it is the one
 * that resolves a user in the first place. Every service calls
 * {@code findByUsername} to turn the authenticated caller's username into a
 * {@link User} before touching any of that user's data.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Looks up an account by login name.
     *
     * <p>The workhorse of the application: used by
     * {@code FlaraUserDetailsService} during authentication, and by every
     * service to resolve the caller before a query.
     *
     * <p>Returns an {@code Optional} rather than a nullable {@link User} so the
     * caller is forced to decide what an absent account means, which is why the
     * services pair it with {@code orElseThrow}.
     *
     * @param username the login name to find
     * @return the account, or empty if no such username exists
     */
    Optional<User> findByUsername(String username);

    /**
     * Looks up an account by email address.
     *
     * @param email the address to find
     * @return the account, or empty if no such address is registered
     */
    Optional<User> findByEmail(String email);

    /**
     * Whether a username is already taken.
     *
     * <p>Checked during registration so a duplicate returns a clean 409 rather
     * than surfacing as a database constraint violation. Returns a boolean
     * rather than the row, so the database can answer without loading one.
     *
     * @param username the login name to test
     * @return {@code true} if an account already uses it
     */
    boolean existsByUsername(String username);

    /**
     * Whether an email address is already registered.
     *
     * @param email the address to test
     * @return {@code true} if an account already uses it
     */
    boolean existsByEmail(String email);
}
