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
import com.flara.exception.ForbiddenException;
import com.flara.exception.NotFoundException;

import java.time.LocalDate;
import java.util.List;

/**
 * Business rules for the two daily logs, physical and mental.
 *
 * <p>Both entities are handled here rather than in two services because they
 * are the same concept recorded from two angles: one row per user per day,
 * created and edited together by the client. Splitting them would duplicate
 * every method for no gain.
 *
 * <p>Two behaviours differ from the other services and are worth noting:
 * <ul>
 *   <li>The by-date lookups return {@code null} rather than throwing. A day with
 *       no log is an ordinary state, not an error, and the controller turns the
 *       null into a 404.</li>
 *   <li>The update methods carry the stored {@code logDate} forward rather than
 *       taking it from the request body, so editing a log cannot silently move
 *       it to a different day.</li>
 * </ul>
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

    /**
     * Resolves an authenticated username into the persisted user record.
     *
     * <p>Called first by every method below, so all subsequent queries run
     * against a real user id rather than a string taken from the request.
     *
     * @param username the authenticated caller's username
     * @return the matching user
     * @throws UsernameNotFoundException if the account no longer exists
     */
    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // ===============================
    // PHYSICAL LOG
    // ===============================

    /**
     * Saves a physical log for the caller, defaulting the date to today.
     *
     * <p>The owner is set server-side, never taken from the request body.
     *
     * @param username the authenticated caller
     * @param log      the log to persist
     * @return the saved log, including its generated id
     */
    public DailyPhysicalLog savePhysicalLog(String username, DailyPhysicalLog log) {
        User user = getUser(username);
        log.setUser(user);
        if (log.getLogDate() == null) {
            log.setLogDate(LocalDate.now());
        }
        return physicalLogRepository.save(log);
    }

    /**
     * Every physical log the caller has recorded, newest first.
     *
     * @param username the authenticated caller
     * @return that user's physical logs
     */
    public List<DailyPhysicalLog> getPhysicalLogs(String username) {
        User user = getUser(username);
        return physicalLogRepository.findByUserIdOrderByLogDateDesc(user.getId());
    }

    /**
     * The caller's physical log for one day, or {@code null} if there is none.
     *
     * <p>Unwraps the repository's {@code Optional} into a null deliberately: an
     * unlogged day is expected, not exceptional, and the controller translates
     * the null into a 404 rather than an error response.
     *
     * @param username the authenticated caller
     * @param date     the day to fetch
     * @return the log for that day, or {@code null} if none was recorded
     */
    public DailyPhysicalLog getPhysicalLogByDate(String username, LocalDate date) {
        User user = getUser(username);
        return physicalLogRepository.findByUserIdAndLogDate(user.getId(), date)
                .orElse(null);
    }

    /**
     * The caller's physical logs across an inclusive date range, newest first.
     *
     * @param username the authenticated caller
     * @param start    first day of the range, inclusive
     * @param end      last day of the range, inclusive
     * @return logs in the range
     */
    public List<DailyPhysicalLog> getPhysicalLogsByRange(String username, LocalDate start, LocalDate end) {
        User user = getUser(username);
        return physicalLogRepository.findByUserIdAndLogDateBetweenOrderByLogDateDesc(
                user.getId(), start, end);
    }

    /**
     * Replaces an existing physical log.
     *
     * <p>Three values are forced from the server side after the ownership check:
     * the id, the owner, and the log date. Carrying the stored date forward is
     * what stops an edit from relocating a log to a different day, which would
     * otherwise create two logs for one date.
     *
     * @param username the authenticated caller
     * @param logId    id of the log to replace
     * @param updated  the replacement state
     * @return the saved log
     * @throws NotFoundException  if no log has that id
     * @throws ForbiddenException if the log belongs to another user
     */
    public DailyPhysicalLog updatePhysicalLog(String username, Long logId, DailyPhysicalLog updated) {
        User user = getUser(username);
        DailyPhysicalLog existing = physicalLogRepository.findById(logId)
                .orElseThrow(() -> new NotFoundException("Log not found."));

        if (!existing.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have access to this record.");
        }

        updated.setId(logId);
        updated.setUser(user);
        updated.setLogDate(existing.getLogDate());
        return physicalLogRepository.save(updated);
    }

    // ===============================
    // MENTAL LOG
    // ===============================

    /**
     * Saves a mental log for the caller, defaulting the date to today.
     *
     * @param username the authenticated caller
     * @param log      the log to persist
     * @return the saved log, including its generated id
     */
    public DailyMentalLog saveMentalLog(String username, DailyMentalLog log) {
        User user = getUser(username);
        log.setUser(user);
        if (log.getLogDate() == null) {
            log.setLogDate(LocalDate.now());
        }
        return mentalLogRepository.save(log);
    }

    /**
     * Every mental log the caller has recorded, newest first.
     *
     * @param username the authenticated caller
     * @return that user's mental logs
     */
    public List<DailyMentalLog> getMentalLogs(String username) {
        User user = getUser(username);
        return mentalLogRepository.findByUserIdOrderByLogDateDesc(user.getId());
    }

    /**
     * The caller's mental log for one day, or {@code null} if there is none.
     *
     * @param username the authenticated caller
     * @param date     the day to fetch
     * @return the log for that day, or {@code null} if none was recorded
     */
    public DailyMentalLog getMentalLogByDate(String username, LocalDate date) {
        User user = getUser(username);
        return mentalLogRepository.findByUserIdAndLogDate(user.getId(), date)
                .orElse(null);
    }

    /**
     * The caller's mental logs across an inclusive date range, newest first.
     *
     * <p>Pairs with {@link #getPhysicalLogsByRange}: the PDF report pulls both
     * over the same window so the two can be compared day by day.
     *
     * @param username the authenticated caller
     * @param start    first day of the range, inclusive
     * @param end      last day of the range, inclusive
     * @return logs in the range
     */
    public List<DailyMentalLog> getMentalLogsByRange(String username, LocalDate start, LocalDate end) {
        User user = getUser(username);
        return mentalLogRepository.findByUserIdAndLogDateBetweenOrderByLogDateDesc(
                user.getId(), start, end);
    }

    /**
     * Replaces an existing mental log, preserving its original date.
     *
     * @param username the authenticated caller
     * @param logId    id of the log to replace
     * @param updated  the replacement state
     * @return the saved log
     * @throws NotFoundException  if no log has that id
     * @throws ForbiddenException if the log belongs to another user
     */
    public DailyMentalLog updateMentalLog(String username, Long logId, DailyMentalLog updated) {
        User user = getUser(username);
        DailyMentalLog existing = mentalLogRepository.findById(logId)
                .orElseThrow(() -> new NotFoundException("Log not found."));

        if (!existing.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have access to this record.");
        }

        updated.setId(logId);
        updated.setUser(user);
        updated.setLogDate(existing.getLogDate());
        return mentalLogRepository.save(updated);
    }

    /**
     * Deletes a physical log the caller owns.
     *
     * @param username the authenticated caller
     * @param logId    id of the log to delete
     * @throws NotFoundException  if no log has that id
     * @throws ForbiddenException if the log belongs to another user
     */
    public void deletePhysicalLog(String username, Long logId) {
        User user = getUser(username);
        DailyPhysicalLog existing = physicalLogRepository.findById(logId)
                .orElseThrow(() -> new NotFoundException("Log not found."));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have access to this record.");
        }
        physicalLogRepository.deleteById(logId);
    }

    /**
     * Deletes a mental log the caller owns.
     *
     * @param username the authenticated caller
     * @param logId    id of the log to delete
     * @throws NotFoundException  if no log has that id
     * @throws ForbiddenException if the log belongs to another user
     */
    public void deleteMentalLog(String username, Long logId) {
        User user = getUser(username);
        DailyMentalLog existing = mentalLogRepository.findById(logId)
                .orElseThrow(() -> new NotFoundException("Log not found."));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have access to this record.");
        }
        mentalLogRepository.deleteById(logId);
    }
}
