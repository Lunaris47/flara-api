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

@RestController
@RequestMapping("/api/meals")
@RequiredArgsConstructor
public class MealLogController {

    private final MealLogService mealLogService;

    @PostMapping
    public ResponseEntity<MealLog> saveMealLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody MealLog log) {
        return ResponseEntity.ok(mealLogService.saveMealLog(userDetails.getUsername(), log));
    }

    @GetMapping
    public ResponseEntity<List<MealLog>> getMealLogs(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(mealLogService.getMealLogs(userDetails.getUsername()));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<MealLog>> getMealLogsByDate(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(mealLogService.getMealLogsByDate(userDetails.getUsername(), date));
    }

    @GetMapping("/range")
    public ResponseEntity<List<MealLog>> getMealLogsByRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(mealLogService.getMealLogsByRange(
                userDetails.getUsername(), start, end));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MealLog> updateMealLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody MealLog log) {
        return ResponseEntity.ok(mealLogService.updateMealLog(userDetails.getUsername(), id, log));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMealLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        mealLogService.deleteMealLog(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}