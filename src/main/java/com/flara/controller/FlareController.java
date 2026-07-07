package com.flara.controller;

import com.flara.entity.Flare;
import com.flara.service.FlareService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/flares")
@RequiredArgsConstructor
public class FlareController {

    private final FlareService flareService;

    @PostMapping
    public ResponseEntity<Flare> saveFlare(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Flare flare) {
        return ResponseEntity.ok(flareService.saveFlare(userDetails.getUsername(), flare));
    }

    @GetMapping
    public ResponseEntity<List<Flare>> getFlares(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(flareService.getFlares(userDetails.getUsername()));
    }

    @GetMapping("/range")
    public ResponseEntity<List<Flare>> getFlaresByRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(flareService.getFlaresByRange(
                userDetails.getUsername(), start, end));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Flare> updateFlare(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody Flare flare) {
        return ResponseEntity.ok(flareService.updateFlare(userDetails.getUsername(), id, flare));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlare(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        flareService.deleteFlare(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}