package com.flara.controller;

import com.flara.entity.DailyMentalLog;
import com.flara.entity.DailyPhysicalLog;
import com.flara.service.DailyLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * HTTP endpoints for the two daily logs, under {@code /api/logs}.
 *
 * <p>Physical and mental logs share one controller because they are the same
 * concept recorded from two angles: one row per user per day, created and
 * edited together by the client. They are separated by path prefix,
 * {@code /physical} and {@code /mental}, with a parallel set of operations on
 * each.
 *
 * <p>The by-date endpoints behave differently from everything else in the API:
 * they return 404 when no log exists for that day, rather than treating the
 * absence as an error. A day the user did not log is an ordinary state.
 */
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class DailyLogController {

    private final DailyLogService dailyLogService;

    // ===============================
    // PHYSICAL LOG ENDPOINTS
    // ===============================

    /**
     * Records a physical log. {@code POST /api/logs/physical}
     *
     * <p>The date defaults to today if the body omits it.
     *
     * @param userDetails the authenticated caller
     * @param log         the log to record
     * @return the saved log, including its generated id
     */
    @PostMapping("/physical")
    public ResponseEntity<DailyPhysicalLog> savePhysicalLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DailyPhysicalLog log) {
        return ResponseEntity.ok(dailyLogService.savePhysicalLog(userDetails.getUsername(), log));
    }

    /**
     * Every physical log the caller has recorded, newest first.
     * {@code GET /api/logs/physical}
     *
     * @param userDetails the authenticated caller
     * @return the caller's physical logs
     */
    @GetMapping("/physical")
    public ResponseEntity<List<DailyPhysicalLog>> getPhysicalLogs(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(dailyLogService.getPhysicalLogs(userDetails.getUsername()));
    }

    /**
     * The physical log for one day.
     * {@code GET /api/logs/physical/date/{date}}
     *
     * <p>The service returns {@code null} when the user did not log that day,
     * and this converts it to a 404. The null is an expected result rather than
     * a failure, which is why it is not thrown as an exception.
     *
     * @param userDetails the authenticated caller
     * @param date        the day to fetch, ISO format
     * @return the log, or 404 if none was recorded that day
     */
    @GetMapping("/physical/date/{date}")
    public ResponseEntity<DailyPhysicalLog> getPhysicalLogByDate(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailyPhysicalLog log = dailyLogService.getPhysicalLogByDate(userDetails.getUsername(), date);
        if (log == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(log);
    }

    /**
     * Physical logs across a date range.
     * {@code GET /api/logs/physical/range}
     *
     * @param userDetails the authenticated caller
     * @param start       first day, inclusive
     * @param end         last day, inclusive
     * @return logs in the range, newest first
     */
    @GetMapping("/physical/range")
    public ResponseEntity<List<DailyPhysicalLog>> getPhysicalLogsByRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(dailyLogService.getPhysicalLogsByRange(
                userDetails.getUsername(), start, end));
    }

    /**
     * Replaces a physical log the caller owns.
     * {@code PUT /api/logs/physical/{id}}
     *
     * <p>The service carries the stored log date forward rather than taking it
     * from the body, so an edit cannot move a log to a different day and create
     * a duplicate for that date.
     *
     * @param userDetails the authenticated caller
     * @param id          id of the log to replace
     * @param log         the replacement state
     * @return the saved log
     */
    @PutMapping("/physical/{id}")
    public ResponseEntity<DailyPhysicalLog> updatePhysicalLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody DailyPhysicalLog log) {
        return ResponseEntity.ok(dailyLogService.updatePhysicalLog(
                userDetails.getUsername(), id, log));
    }

    // ===============================
    // MENTAL LOG ENDPOINTS
    // ===============================

    /**
     * Records a mental log. {@code POST /api/logs/mental}
     *
     * <p>The date defaults to today if the body omits it.
     *
     * @param userDetails the authenticated caller
     * @param log         the log to record
     * @return the saved log, including its generated id
     */
    @PostMapping("/mental")
    public ResponseEntity<DailyMentalLog> saveMentalLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DailyMentalLog log) {
        return ResponseEntity.ok(dailyLogService.saveMentalLog(userDetails.getUsername(), log));
    }

    /**
     * Every mental log the caller has recorded, newest first.
     * {@code GET /api/logs/mental}
     *
     * @param userDetails the authenticated caller
     * @return the caller's mental logs
     */
    @GetMapping("/mental")
    public ResponseEntity<List<DailyMentalLog>> getMentalLogs(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(dailyLogService.getMentalLogs(userDetails.getUsername()));
    }

    /**
     * The mental log for one day.
     * {@code GET /api/logs/mental/date/{date}}
     *
     * @param userDetails the authenticated caller
     * @param date        the day to fetch, ISO format
     * @return the log, or 404 if none was recorded that day
     */
    @GetMapping("/mental/date/{date}")
    public ResponseEntity<DailyMentalLog> getMentalLogByDate(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailyMentalLog log = dailyLogService.getMentalLogByDate(userDetails.getUsername(), date);
        if (log == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(log);
    }

    /**
     * Mental logs across a date range. {@code GET /api/logs/mental/range}
     *
     * <p>Pairs with the physical equivalent: fetching both over the same window
     * is what lets the client line mental state up against symptoms.
     *
     * @param userDetails the authenticated caller
     * @param start       first day, inclusive
     * @param end         last day, inclusive
     * @return logs in the range, newest first
     */
    @GetMapping("/mental/range")
    public ResponseEntity<List<DailyMentalLog>> getMentalLogsByRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(dailyLogService.getMentalLogsByRange(
                userDetails.getUsername(), start, end));
    }

    /**
     * Replaces a mental log the caller owns, preserving its original date.
     * {@code PUT /api/logs/mental/{id}}
     *
     * @param userDetails the authenticated caller
     * @param id          id of the log to replace
     * @param log         the replacement state
     * @return the saved log
     */
    @PutMapping("/mental/{id}")
    public ResponseEntity<DailyMentalLog> updateMentalLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody DailyMentalLog log) {
        return ResponseEntity.ok(dailyLogService.updateMentalLog(
                userDetails.getUsername(), id, log));
    }

    /**
     * Deletes a physical log the caller owns.
     * {@code DELETE /api/logs/physical/{id}}
     *
     * @param userDetails the authenticated caller
     * @param id          id of the log to delete
     * @return an empty 204 response
     */
    @DeleteMapping("/physical/{id}")
    public ResponseEntity<Void> deletePhysicalLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        dailyLogService.deletePhysicalLog(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes a mental log the caller owns.
     * {@code DELETE /api/logs/mental/{id}}
     *
     * @param userDetails the authenticated caller
     * @param id          id of the log to delete
     * @return an empty 204 response
     */
    @DeleteMapping("/mental/{id}")
    public ResponseEntity<Void> deleteMentalLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        dailyLogService.deleteMentalLog(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}