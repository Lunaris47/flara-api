package com.flara.service;

import com.flara.entity.DailyMentalLog;
import com.flara.entity.DailyPhysicalLog;
import com.flara.entity.User;
import com.flara.repository.DailyMentalLogRepository;
import com.flara.repository.DailyPhysicalLogRepository;
import com.flara.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Handles saving and retrieving daily physical and mental logs.
 */
@Service
@RequiredArgsConstructor
public class DailyLogService {

    private final DailyPhysicalLogRepository physicalLogRepository;
    private final DailyMentalLogRepository mentalLogRepository;
    private final UserRepository userRepository;

    // ===============================
    // HELPER
    // ===============================

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // ===============================
    // PHYSICAL LOG
    // ===============================

    public DailyPhysicalLog savePhysicalLog(String username, DailyPhysicalLog log) {
        User user = getUser(username);
        log.setUser(user);
        if (log.getLogDate() == null) {
            log.setLogDate(LocalDate.now());
        }
        return physicalLogRepository.save(log);
    }

    public List<DailyPhysicalLog> getPhysicalLogs(String username) {
        User user = getUser(username);
        return physicalLogRepository.findByUserIdOrderByLogDateDesc(user.getId());
    }

    public DailyPhysicalLog getPhysicalLogByDate(String username, LocalDate date) {
        User user = getUser(username);
        return physicalLogRepository.findByUserIdAndLogDate(user.getId(), date)
                .orElse(null);
    }

    public List<DailyPhysicalLog> getPhysicalLogsByRange(String username, LocalDate start, LocalDate end) {
        User user = getUser(username);
        return physicalLogRepository.findByUserIdAndLogDateBetweenOrderByLogDateDesc(
                user.getId(), start, end);
    }

    public DailyPhysicalLog updatePhysicalLog(String username, Long logId, DailyPhysicalLog updated) {
        User user = getUser(username);
        DailyPhysicalLog existing = physicalLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Log not found."));

        if (!existing.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized.");
        }

        updated.setId(logId);
        updated.setUser(user);
        updated.setLogDate(existing.getLogDate());
        return physicalLogRepository.save(updated);
    }

    // ===============================
    // MENTAL LOG
    // ===============================

    public DailyMentalLog saveMentalLog(String username, DailyMentalLog log) {
        User user = getUser(username);
        log.setUser(user);
        if (log.getLogDate() == null) {
            log.setLogDate(LocalDate.now());
        }
        return mentalLogRepository.save(log);
    }

    public List<DailyMentalLog> getMentalLogs(String username) {
        User user = getUser(username);
        return mentalLogRepository.findByUserIdOrderByLogDateDesc(user.getId());
    }

    public DailyMentalLog getMentalLogByDate(String username, LocalDate date) {
        User user = getUser(username);
        return mentalLogRepository.findByUserIdAndLogDate(user.getId(), date)
                .orElse(null);
    }

    public List<DailyMentalLog> getMentalLogsByRange(String username, LocalDate start, LocalDate end) {
        User user = getUser(username);
        return mentalLogRepository.findByUserIdAndLogDateBetweenOrderByLogDateDesc(
                user.getId(), start, end);
    }

    public DailyMentalLog updateMentalLog(String username, Long logId, DailyMentalLog updated) {
        User user = getUser(username);
        DailyMentalLog existing = mentalLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Log not found."));

        if (!existing.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized.");
        }

        updated.setId(logId);
        updated.setUser(user);
        updated.setLogDate(existing.getLogDate());
        return mentalLogRepository.save(updated);
    }

    public void deletePhysicalLog(String username, Long logId) {
        User user = getUser(username);
        DailyPhysicalLog existing = physicalLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Log not found."));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized.");
        }
        physicalLogRepository.deleteById(logId);
    }

    public void deleteMentalLog(String username, Long logId) {
        User user = getUser(username);
        DailyMentalLog existing = mentalLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Log not found."));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized.");
        }
        mentalLogRepository.deleteById(logId);
    }
}