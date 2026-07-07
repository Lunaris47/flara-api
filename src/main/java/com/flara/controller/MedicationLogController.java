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

@RestController
@RequestMapping("/api/medications")
@RequiredArgsConstructor
public class MedicationLogController {

    private final MedicationLogService medicationLogService;

    @PostMapping
    public ResponseEntity<MedicationLog> saveMedicationLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody MedicationLog log) {
        return ResponseEntity.ok(medicationLogService.saveMedicationLog(
                userDetails.getUsername(), log));
    }

    @GetMapping
    public ResponseEntity<List<MedicationLog>> getMedicationLogs(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(medicationLogService.getMedicationLogs(userDetails.getUsername()));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<MedicationLog>> getMedicationLogsByDate(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(medicationLogService.getMedicationLogsByDate(
                userDetails.getUsername(), date));
    }

    @GetMapping("/range")
    public ResponseEntity<List<MedicationLog>> getMedicationLogsByRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(medicationLogService.getMedicationLogsByRange(
                userDetails.getUsername(), start, end));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicationLog> updateMedicationLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody MedicationLog log) {
        return ResponseEntity.ok(medicationLogService.updateMedicationLog(
                userDetails.getUsername(), id, log));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicationLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        medicationLogService.deleteMedicationLog(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}