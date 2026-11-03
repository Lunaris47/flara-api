package com.flara.controller;

import com.flara.entity.MedicationLog;
import com.flara.service.MedicationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * HTTP endpoints for medication logs, under {@code /api/medications}.
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
@RequestMapping("/api/medications")
@RequiredArgsConstructor
public class MedicationLogController {

    private final MedicationLogService medicationLogService;

    /**
     * Records a dose, taken or missed. {@code POST /api/medications}
     *
     * <p>A dose marked not taken is data, not an omission: non-adherence is
     * exactly what the doctor's report needs to show.
     *
     * @param userDetails the authenticated caller
     * @param log         the dose record
     * @return the saved record, including its generated id
     */
    @PostMapping
    public ResponseEntity<MedicationLog> saveMedicationLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody MedicationLog log) {
        return ResponseEntity.ok(medicationLogService.saveMedicationLog(
                userDetails.getUsername(), log));
    }

    /**
     * Every dose the caller has logged, newest day first.
     * {@code GET /api/medications}
     *
     * @param userDetails the authenticated caller
     * @return the caller's medication logs
     */
    @GetMapping
    public ResponseEntity<List<MedicationLog>> getMedicationLogs(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(medicationLogService.getMedicationLogs(userDetails.getUsername()));
    }

    /**
     * Doses for one day. {@code GET /api/medications/date/{date}}
     *
     * <p>A list, since a user may take several medications in a day.
     *
     * @param userDetails the authenticated caller
     * @param date        the day to fetch, ISO format
     * @return that day's doses, possibly empty
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<List<MedicationLog>> getMedicationLogsByDate(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(medicationLogService.getMedicationLogsByDate(
                userDetails.getUsername(), date));
    }

    /**
     * Doses within a date range. {@code GET /api/medications/range}
     *
     * @param userDetails the authenticated caller
     * @param start       first day, inclusive
     * @param end         last day, inclusive
     * @return doses in the range, newest first
     */
    @GetMapping("/range")
    public ResponseEntity<List<MedicationLog>> getMedicationLogsByRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(medicationLogService.getMedicationLogsByRange(
                userDetails.getUsername(), start, end));
    }

    /**
     * Replaces a dose record the caller owns.
     * {@code PUT /api/medications/{id}}
     *
     * @param userDetails the authenticated caller
     * @param id          id of the record to replace
     * @param log         the replacement state
     * @return the saved record
     */
    @PutMapping("/{id}")
    public ResponseEntity<MedicationLog> updateMedicationLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody MedicationLog log) {
        return ResponseEntity.ok(medicationLogService.updateMedicationLog(
                userDetails.getUsername(), id, log));
    }

    /**
     * Deletes a dose record the caller owns.
     * {@code DELETE /api/medications/{id}}
     *
     * @param userDetails the authenticated caller
     * @param id          id of the record to delete
     * @return an empty 204 response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicationLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        medicationLogService.deleteMedicationLog(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}