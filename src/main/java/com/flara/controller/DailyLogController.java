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
 * Handles daily physical and mental log endpoints.
 */
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class DailyLogController {

    private final DailyLogService dailyLogService;

    // ===============================
    // PHYSICAL LOG ENDPOINTS
    // ===============================

    @PostMapping("/physical")
    public ResponseEntity<DailyPhysicalLog> savePhysicalLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DailyPhysicalLog log) {
        return ResponseEntity.ok(dailyLogService.savePhysicalLog(userDetails.getUsername(), log));
    }

    @GetMapping("/physical")
    public ResponseEntity<List<DailyPhysicalLog>> getPhysicalLogs(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(dailyLogService.getPhysicalLogs(userDetails.getUsername()));
    }

    @GetMapping("/physical/date/{date}")
    public ResponseEntity<DailyPhysicalLog> getPhysicalLogByDate(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailyPhysicalLog log = dailyLogService.getPhysicalLogByDate(userDetails.getUsername(), date);
        if (log == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(log);
    }

    @GetMapping("/physical/range")
    public ResponseEntity<List<DailyPhysicalLog>> getPhysicalLogsByRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(dailyLogService.getPhysicalLogsByRange(
                userDetails.getUsername(), start, end));
    }

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

    @PostMapping("/mental")
    public ResponseEntity<DailyMentalLog> saveMentalLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DailyMentalLog log) {
        return ResponseEntity.ok(dailyLogService.saveMentalLog(userDetails.getUsername(), log));
    }

    @GetMapping("/mental")
    public ResponseEntity<List<DailyMentalLog>> getMentalLogs(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(dailyLogService.getMentalLogs(userDetails.getUsername()));
    }

    @GetMapping("/mental/date/{date}")
    public ResponseEntity<DailyMentalLog> getMentalLogByDate(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailyMentalLog log = dailyLogService.getMentalLogByDate(userDetails.getUsername(), date);
        if (log == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(log);
    }

    @GetMapping("/mental/range")
    public ResponseEntity<List<DailyMentalLog>> getMentalLogsByRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(dailyLogService.getMentalLogsByRange(
                userDetails.getUsername(), start, end));
    }

    @PutMapping("/mental/{id}")
    public ResponseEntity<DailyMentalLog> updateMentalLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody DailyMentalLog log) {
        return ResponseEntity.ok(dailyLogService.updateMentalLog(
                userDetails.getUsername(), id, log));
    }
}