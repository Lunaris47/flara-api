package com.flara.controller;

import com.flara.entity.MealLog;
import com.flara.service.MealLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * HTTP endpoints for meal logs, under {@code /api/meals}.
 *
 * <p>Thin by design: each method unwraps the request, calls the service and
 * wraps the result. The rules, including the ownership checks on update and
 * delete, live one layer down.
 *
 * <p>{@code @AuthenticationPrincipal} supplies the caller established by
 * {@code JwtFilter}. Only the username travels downward, so no client-supplied
 * identifier can reach a query.
 */
@RestController
@RequestMapping("/api/meals")
@RequiredArgsConstructor
public class MealLogController {

    private final MealLogService mealLogService;

    /**
     * Records a meal. {@code POST /api/meals}
     *
     * @param userDetails the authenticated caller
     * @param log         the meal to record
     * @return the saved meal, including its generated id
     */
    @PostMapping
    public ResponseEntity<MealLog> saveMealLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody MealLog log) {
        return ResponseEntity.ok(mealLogService.saveMealLog(userDetails.getUsername(), log));
    }

    /**
     * Every meal the caller has logged, newest day first. {@code GET /api/meals}
     *
     * @param userDetails the authenticated caller
     * @return the caller's meals
     */
    @GetMapping
    public ResponseEntity<List<MealLog>> getMealLogs(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(mealLogService.getMealLogs(userDetails.getUsername()));
    }

    /**
     * Meals logged on one day. {@code GET /api/meals/date/{date}}
     *
     * <p>Returns a list rather than a single record, since several meals are
     * logged per day. An empty list means nothing was recorded, which is a
     * normal state rather than a 404.
     *
     * @param userDetails the authenticated caller
     * @param date        the day to fetch, ISO format
     * @return that day's meals, possibly empty
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<List<MealLog>> getMealLogsByDate(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(mealLogService.getMealLogsByDate(userDetails.getUsername(), date));
    }

    /**
     * Meals within a date range. {@code GET /api/meals/range}
     *
     * @param userDetails the authenticated caller
     * @param start       first day, inclusive
     * @param end         last day, inclusive
     * @return meals in the range, newest first
     */
    @GetMapping("/range")
    public ResponseEntity<List<MealLog>> getMealLogsByRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(mealLogService.getMealLogsByRange(
                userDetails.getUsername(), start, end));
    }

    /**
     * Replaces a meal the caller owns. {@code PUT /api/meals/{id}}
     *
     * <p>A full replacement: fields absent from the body are cleared. The
     * service returns 403 if the record belongs to another user.
     *
     * @param userDetails the authenticated caller
     * @param id          id of the meal to replace
     * @param log         the replacement state
     * @return the saved meal
     */
    @PutMapping("/{id}")
    public ResponseEntity<MealLog> updateMealLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody MealLog log) {
        return ResponseEntity.ok(mealLogService.updateMealLog(userDetails.getUsername(), id, log));
    }

    /**
     * Deletes a meal the caller owns. {@code DELETE /api/meals/{id}}
     *
     * @param userDetails the authenticated caller
     * @param id          id of the meal to delete
     * @return an empty 204 response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMealLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        mealLogService.deleteMealLog(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}