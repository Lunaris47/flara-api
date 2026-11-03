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
import org.springframework.transaction.annotation.Transactional;

/**
 * Account management, under {@code /api/user}.
 *
 * <p>The exception to the thin-controller rule followed everywhere else in this
 * package: there is no {@code UserService}, so the deletion logic and all six
 * repositories live here. That is a known wart. The logic belongs in a service,
 * both for consistency and so it could be unit tested.
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
    /**
     * Permanently deletes the caller's account and everything they have logged.
     * {@code DELETE /api/user/delete}
     *
     * <p>Child rows must go before the user row, because each log entity holds a
     * foreign key to {@code users}; deleting the parent first would violate the
     * constraint.
     *
     * <p>{@code @Transactional} makes all six deletions one unit of work. Without
     * it, a failure partway through would leave the account intact but its data
     * partially destroyed, which is the worst possible outcome for an operation
     * the user cannot undo.
     *
     * <p>Known inefficiency: each repository loads every matching row into
     * memory before deleting it. A derived {@code deleteByUserId} query would do
     * the same work in six statements without materializing anything.
     *
     * @param userDetails the authenticated caller, whose own account is deleted
     * @return an empty 204 response
     */
    @DeleteMapping("/delete")
    @Transactional
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Child rows first: every log table holds a foreign key to users, so
        // removing the parent before them would violate the constraint.
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

        // Finally the account itself.
        userRepository.delete(user);

        return ResponseEntity.noContent().build();
    }
}