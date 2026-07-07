package com.flara.service;

import com.flara.entity.MedicationLog;
import com.flara.entity.User;
import com.flara.repository.MedicationLogRepository;
import com.flara.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Handles saving and retrieving medication logs.
 */
@Service
@RequiredArgsConstructor
public class MedicationLogService {

    private final MedicationLogRepository medicationLogRepository;
    private final UserRepository userRepository;

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public MedicationLog saveMedicationLog(String username, MedicationLog log) {
        User user = getUser(username);
        log.setUser(user);
        if (log.getLogDate() == null) log.setLogDate(LocalDate.now());
        return medicationLogRepository.save(log);
    }

    public List<MedicationLog> getMedicationLogs(String username) {
        User user = getUser(username);
        return medicationLogRepository.findByUserIdOrderByLogDateDesc(user.getId());
    }

    public List<MedicationLog> getMedicationLogsByDate(String username, LocalDate date) {
        User user = getUser(username);
        return medicationLogRepository.findByUserIdAndLogDate(user.getId(), date);
    }

    public List<MedicationLog> getMedicationLogsByRange(String username, LocalDate start, LocalDate end) {
        User user = getUser(username);
        return medicationLogRepository.findByUserIdAndLogDateBetweenOrderByLogDateDesc(
                user.getId(), start, end);
    }

    public MedicationLog updateMedicationLog(String username, Long logId, MedicationLog updated) {
        User user = getUser(username);
        MedicationLog existing = medicationLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Medication log not found."));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized.");
        }
        updated.setId(logId);
        updated.setUser(user);
        return medicationLogRepository.save(updated);
    }

    public void deleteMedicationLog(String username, Long logId) {
        User user = getUser(username);
        MedicationLog existing = medicationLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Medication log not found."));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized.");
        }
        medicationLogRepository.deleteById(logId);
    }
}