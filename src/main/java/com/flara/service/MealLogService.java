package com.flara.service;

import com.flara.entity.MealLog;
import com.flara.entity.User;
import com.flara.repository.MealLogRepository;
import com.flara.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Handles saving and retrieving meal logs.
 */
@Service
@RequiredArgsConstructor
public class MealLogService {

    private final MealLogRepository mealLogRepository;
    private final UserRepository userRepository;

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public MealLog saveMealLog(String username, MealLog log) {
        User user = getUser(username);
        log.setUser(user);
        if (log.getLogDate() == null) log.setLogDate(LocalDate.now());
        return mealLogRepository.save(log);
    }

    public List<MealLog> getMealLogs(String username) {
        User user = getUser(username);
        return mealLogRepository.findByUserIdOrderByLogDateDesc(user.getId());
    }

    public List<MealLog> getMealLogsByDate(String username, LocalDate date) {
        User user = getUser(username);
        return mealLogRepository.findByUserIdAndLogDate(user.getId(), date);
    }

    public List<MealLog> getMealLogsByRange(String username, LocalDate start, LocalDate end) {
        User user = getUser(username);
        return mealLogRepository.findByUserIdAndLogDateBetweenOrderByLogDateDesc(
                user.getId(), start, end);
    }

    public MealLog updateMealLog(String username, Long logId, MealLog updated) {
        User user = getUser(username);
        MealLog existing = mealLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Meal log not found."));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized.");
        }
        updated.setId(logId);
        updated.setUser(user);
        return mealLogRepository.save(updated);
    }

    public void deleteMealLog(String username, Long logId) {
        User user = getUser(username);
        MealLog existing = mealLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Meal log not found."));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized.");
        }
        mealLogRepository.deleteById(logId);
    }
}