package com.flara.service;

import com.flara.entity.Flare;
import com.flara.entity.User;
import com.flara.repository.FlareRepository;
import com.flara.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.flara.exception.ForbiddenException;
import com.flara.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Business rules for flare episodes.
 *
 * <p>This is where authorization actually happens. Spring Security establishes
 * <em>who</em> is calling; this class decides whether that caller may touch a
 * particular record. Being authenticated is not the same as being entitled, and
 * the ownership checks in {@link #updateFlare} and {@link #deleteFlare} are what
 * enforce the difference.
 *
 * <p>Every public method takes a username rather than a user id. The controller
 * passes the authenticated principal's username and nothing else, so a
 * client-supplied identifier can never reach a query.
 *
 * <p>Dependencies are injected through the constructor Lombok generates, which
 * is what allows the repositories to be replaced with mocks in a unit test.
 */
@Service
@RequiredArgsConstructor
public class FlareService {

    private final FlareRepository flareRepository;
    private final UserRepository userRepository;

    /**
     * Resolves an authenticated username into the persisted user record.
     *
     * <p>Called first by every method here, so all subsequent queries are scoped
     * to a real user id rather than to a string from the request.
     *
     * @param username the authenticated caller's username
     * @return the matching user
     * @throws UsernameNotFoundException if the account has been deleted since
     *                                   the token was issued
     */
    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Saves a new flare for the caller.
     *
     * <p>The owner is set server-side from the authenticated username rather
     * than taken from the request body, so a client cannot create a flare
     * belonging to somebody else. A missing start date defaults to now, which
     * supports the common case of logging a flare as it begins.
     *
     * @param username the authenticated caller
     * @param flare    the flare to persist
     * @return the saved flare, including its generated id
     */
    public Flare saveFlare(String username, Flare flare) {
        User user = getUser(username);
        flare.setUser(user);
        if (flare.getStartDate() == null) flare.setStartDate(LocalDateTime.now());
        return flareRepository.save(flare);
    }

    /**
     * All of the caller's flares, most recent first.
     *
     * @param username the authenticated caller
     * @return that user's flares, newest first
     */
    public List<Flare> getFlares(String username) {
        User user = getUser(username);
        return flareRepository.findByUserIdOrderByStartDateDesc(user.getId());
    }

    /**
     * The caller's flares that began within a window, most recent first.
     *
     * @param username the authenticated caller
     * @param start    beginning of the window, inclusive
     * @param end      end of the window, inclusive
     * @return matching flares, newest first
     */
    public List<Flare> getFlaresByRange(String username, LocalDateTime start, LocalDateTime end) {
        User user = getUser(username);
        return flareRepository.findByUserIdAndStartDateBetweenOrderByStartDateDesc(
                user.getId(), start, end);
    }

    /**
     * Replaces an existing flare.
     *
     * <p>Two safeguards, and they do different jobs:
     * <ul>
     *   <li>The <b>ownership check</b> loads the stored flare and compares its
     *       owner to the caller. A valid token for another account produces a
     *       {@link ForbiddenException}, not a silent modification.</li>
     *   <li>The id and owner on the incoming object are <b>overwritten
     *       server-side</b> before saving, so a client cannot reassign a flare
     *       to a different user or a different id by editing the JSON.</li>
     * </ul>
     *
     * <p>This is a full replacement, matching PUT semantics: fields absent from
     * the request body are cleared, not preserved.
     *
     * @param username the authenticated caller
     * @param flareId  id of the flare to replace
     * @param updated  the replacement state
     * @return the saved flare
     * @throws NotFoundException  if no flare has that id
     * @throws ForbiddenException if the flare belongs to another user
     */
    public Flare updateFlare(String username, Long flareId, Flare updated) {
        User user = getUser(username);
        Flare existing = flareRepository.findById(flareId)
                .orElseThrow(() -> new NotFoundException("Flare not found."));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have access to this record.");
        }
        updated.setId(flareId);
        updated.setUser(user);
        return flareRepository.save(updated);
    }

    /**
     * Deletes a flare the caller owns.
     *
     * <p>The record is loaded before deletion specifically so its owner can be
     * checked. Deleting by id alone would let any authenticated user remove any
     * flare in the system.
     *
     * @param username the authenticated caller
     * @param flareId  id of the flare to delete
     * @throws NotFoundException  if no flare has that id
     * @throws ForbiddenException if the flare belongs to another user
     */
    public void deleteFlare(String username, Long flareId) {
        User user = getUser(username);
        Flare existing = flareRepository.findById(flareId)
                .orElseThrow(() -> new NotFoundException("Flare not found."));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have access to this record.");
        }
        flareRepository.deleteById(flareId);
    }
}
