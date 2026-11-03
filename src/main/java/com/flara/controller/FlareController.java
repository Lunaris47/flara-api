package com.flara.controller;

import com.flara.entity.Flare;
import com.flara.service.FlareService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HTTP endpoints for flare episodes, under {@code /api/flares}.
 *
 * <p>Deliberately thin. Each method unwraps the request, calls
 * {@link FlareService} and wraps the result. No rules live here: putting them
 * in the service means they can be unit tested with plain method calls instead
 * of HTTP round trips.
 *
 * <p>{@code @AuthenticationPrincipal} supplies the user that {@code JwtFilter}
 * authenticated earlier in the chain. Only the username is passed downward, so
 * a client-supplied user identifier can never reach a query.
 */
@RestController
@RequestMapping("/api/flares")
@RequiredArgsConstructor
public class FlareController {

    private final FlareService flareService;

    /**
     * Records a new flare. {@code POST /api/flares}
     *
     * @param userDetails the authenticated caller, injected by Spring Security
     * @param flare       the flare to record
     * @return the saved flare, including its generated id
     */
    @PostMapping
    public ResponseEntity<Flare> saveFlare(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Flare flare) {
        return ResponseEntity.ok(flareService.saveFlare(userDetails.getUsername(), flare));
    }

    /**
     * Every flare belonging to the caller, newest first. {@code GET /api/flares}
     *
     * @param userDetails the authenticated caller
     * @return the caller's flares
     */
    @GetMapping
    public ResponseEntity<List<Flare>> getFlares(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(flareService.getFlares(userDetails.getUsername()));
    }

    /**
     * Flares that began within a window. {@code GET /api/flares/range}
     *
     * <p>{@code @DateTimeFormat} tells Spring how to parse the query parameters;
     * without it the ISO date-time strings would fail to bind.
     *
     * @param userDetails the authenticated caller
     * @param start       beginning of the window, ISO date-time
     * @param end         end of the window, ISO date-time
     * @return matching flares, newest first
     */
    @GetMapping("/range")
    public ResponseEntity<List<Flare>> getFlaresByRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(flareService.getFlaresByRange(
                userDetails.getUsername(), start, end));
    }

    /**
     * Replaces a flare the caller owns. {@code PUT /api/flares/{id}}
     *
     * <p>PUT rather than PATCH because this is a full replacement: fields absent
     * from the body are cleared, not preserved. The service verifies ownership
     * and returns 403 if the flare belongs to another user.
     *
     * @param userDetails the authenticated caller
     * @param id          id of the flare to replace
     * @param flare       the replacement state
     * @return the saved flare
     */
    @PutMapping("/{id}")
    public ResponseEntity<Flare> updateFlare(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody Flare flare) {
        return ResponseEntity.ok(flareService.updateFlare(userDetails.getUsername(), id, flare));
    }

    /**
     * Deletes a flare the caller owns. {@code DELETE /api/flares/{id}}
     *
     * <p>Returns 204 No Content: the deletion succeeded and there is nothing
     * meaningful to send back.
     *
     * @param userDetails the authenticated caller
     * @param id          id of the flare to delete
     * @return an empty 204 response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlare(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        flareService.deleteFlare(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}