package com.flara.controller;

import com.flara.repository.DailyMentalLogRepository;
import com.flara.repository.DailyPhysicalLogRepository;
import com.flara.repository.FlareRepository;
import com.flara.repository.MealLogRepository;
import com.flara.repository.MedicationLogRepository;
import com.flara.repository.UserRepository;
import com.flara.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

/**
 * Handles user account management endpoints.
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final DailyPhysicalLogRepository physicalLogRepository;
    private final DailyMentalLogRepository mentalLogRepository;
    private final MealLogRepository mealLogRepository;
    private final MedicationLogRepository medicationLogRepository;
    private final FlareRepository flareRepository;

    /**
     * Permanently deletes the user account and all associated data.
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Delete all user data first (foreign key constraints)
        physicalLogRepository.deleteAll(
                physicalLogRepository.findByUserIdOrderByLogDateDesc(user.getId()));
        mentalLogRepository.deleteAll(
                mentalLogRepository.findByUserIdOrderByLogDateDesc(user.getId()));
        mealLogRepository.deleteAll(
                mealLogRepository.findByUserIdOrderByLogDateDesc(user.getId()));
        medicationLogRepository.deleteAll(
                medicationLogRepository.findByUserIdOrderByLogDateDesc(user.getId()));
        flareRepository.deleteAll(
                flareRepository.findByUserIdOrderByStartDateDesc(user.getId()));

        // Delete the user
        userRepository.delete(user);

        return ResponseEntity.noContent().build();
    }
}