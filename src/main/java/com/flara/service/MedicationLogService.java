package com.flara.service;

import com.flara.entity.MedicationLog;
import com.flara.entity.User;
import com.flara.repository.MedicationLogRepository;
import com.flara.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.flara.exception.ForbiddenException;
import com.flara.exception.NotFoundException;

import java.time.LocalDate;
import java.util.List;

/**
 * Business rules for medication logs.
 *
 * <p>Follows the same template as {@code FlareService} and
 * {@code MealLogService}: resolve the caller first, scope every read by their
 * user id, and verify ownership before writing to an existing record.
 *
 * <p>Note that a dose marked not taken is still a record worth keeping. Nothing
 * here deletes or suppresses such rows, because non-adherence is exactly what a
 * clinician needs to see in the report.
 */
@Service
@RequiredArgsConstructor
public class MedicationLogService {

    private final MedicationLogRepository medicationLogRepository;
    private final UserRepository userRepository;

    /**
     * Resolves an authenticated username into the persisted user record.
     *
     * @param username the authenticated caller's username
     * @return the matching user
     * @throws UsernameNotFoundException if the account no longer exists
     */
    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Saves a new medication log for the caller.
     *
     * <p>The owner is set server-side, never taken from the request body. A
     * missing log date defaults to today.
     *
     * @param username the authenticated caller
     * @param log      the dose record to persist
     * @return the saved record, including its generated id
     */
    public MedicationLog saveMedicationLog(String username, MedicationLog log) {
        User user = getUser(username);
        log.setUser(user);
        if (log.getLogDate() == null) log.setLogDate(LocalDate.now());
        return medicationLogRepository.save(log);
    }

    /**
     * Every dose the caller has logged, newest day first.
     *
     * @param username the authenticated caller
     * @return that user's medication logs
     */
    public List<MedicationLog> getMedicationLogs(String username) {
        User user = getUser(username);
        return medicationLogRepository.findByUserIdOrderByLogDateDesc(user.getId());
    }

    /**
     * The caller's doses for one day.
     *
     * <p>A list rather than a single record, since a user may take several
     * medications in a day. An empty list means nothing was recorded.
     *
     * @param username the authenticated caller
     * @param date     the day to fetch
     * @return that day's doses, possibly empty
     */
    public List<MedicationLog> getMedicationLogsByDate(String username, LocalDate date) {
        User user = getUser(username);
        return medicationLogRepository.findByUserIdAndLogDate(user.getId(), date);
    }

    /**
     * The caller's doses within an inclusive date range, newest first.
     *
     * @param username the authenticated caller
     * @param start    first day of the range, inclusive
     * @param end      last day of the range, inclusive
     * @return doses in the range
     */
    public List<MedicationLog> getMedicationLogsByRange(String username, LocalDate start, LocalDate end) {
        User user = getUser(username);
        return medicationLogRepository.findByUserIdAndLogDateBetweenOrderByLogDateDesc(
                user.getId(), start, end);
    }

    /**
     * Replaces an existing medication log.
     *
     * <p>Loads the stored record first so its owner can be compared against the
     * caller, then overwrites the id and owner on the incoming object before
     * saving, so neither can be set from the request body.
     *
     * @param username the authenticated caller
     * @param logId    id of the record to replace
     * @param updated  the replacement state
     * @return the saved record
     * @throws NotFoundException  if no medication log has that id
     * @throws ForbiddenException if the record belongs to another user
     */
    public MedicationLog updateMedicationLog(String username, Long logId, MedicationLog updated) {
        User user = getUser(username);
        MedicationLog existing = medicationLogRepository.findById(logId)
                .orElseThrow(() -> new NotFoundException("Medication log not found."));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have access to this record.");
        }
        updated.setId(logId);
        updated.setUser(user);
        return medicationLogRepository.save(updated);
    }

    /**
     * Deletes a medication log the caller owns.
     *
     * <p>The record is loaded before deletion purely so ownership can be
     * verified.
     *
     * @param username the authenticated caller
     * @param logId    id of the record to delete
     * @throws NotFoundException  if no medication log has that id
     * @throws ForbiddenException if the record belongs to another user
     */
    public void deleteMedicationLog(String username, Long logId) {
        User user = getUser(username);
        MedicationLog existing = medicationLogRepository.findById(logId)
                .orElseThrow(() -> new NotFoundException("Medication log not found."));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have access to this record.");
        }
        medicationLogRepository.deleteById(logId);
    }
}
