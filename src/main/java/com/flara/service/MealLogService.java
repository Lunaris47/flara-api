package com.flara.service;

import com.flara.entity.MealLog;
import com.flara.entity.User;
import com.flara.repository.MealLogRepository;
import com.flara.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.flara.exception.ForbiddenException;
import com.flara.exception.NotFoundException;

import java.time.LocalDate;
import java.util.List;

/**
 * Business rules for meal logs.
 *
 * <p>Follows the same template as {@code FlareService}: resolve the caller
 * first, scope every read by their user id, and verify ownership before any
 * write to an existing record. Reads are already safe because the repository
 * queries are scoped by user id; updates and deletes are not, because they
 * start from a client-supplied record id, which is why those two methods carry
 * an explicit check.
 */
@Service
@RequiredArgsConstructor
public class MealLogService {

    private final MealLogRepository mealLogRepository;
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
     * Saves a new meal for the caller.
     *
     * <p>The owner is set server-side, never taken from the request body. A
     * missing log date defaults to today, which covers the common case of
     * recording a meal as it is eaten.
     *
     * @param username the authenticated caller
     * @param log      the meal to persist
     * @return the saved meal, including its generated id
     */
    public MealLog saveMealLog(String username, MealLog log) {
        User user = getUser(username);
        log.setUser(user);
        if (log.getLogDate() == null) log.setLogDate(LocalDate.now());
        return mealLogRepository.save(log);
    }

    /**
     * Every meal the caller has logged, newest day first.
     *
     * @param username the authenticated caller
     * @return that user's meals
     */
    public List<MealLog> getMealLogs(String username) {
        User user = getUser(username);
        return mealLogRepository.findByUserIdOrderByLogDateDesc(user.getId());
    }

    /**
     * The caller's meals for one day.
     *
     * <p>Returns a list rather than a single record, since several meals are
     * logged per day. An empty list means nothing was recorded, which is a
     * normal state rather than an error.
     *
     * @param username the authenticated caller
     * @param date     the day to fetch
     * @return that day's meals, possibly empty
     */
    public List<MealLog> getMealLogsByDate(String username, LocalDate date) {
        User user = getUser(username);
        return mealLogRepository.findByUserIdAndLogDate(user.getId(), date);
    }

    /**
     * The caller's meals within an inclusive date range, newest first.
     *
     * @param username the authenticated caller
     * @param start    first day of the range, inclusive
     * @param end      last day of the range, inclusive
     * @return meals in the range
     */
    public List<MealLog> getMealLogsByRange(String username, LocalDate start, LocalDate end) {
        User user = getUser(username);
        return mealLogRepository.findByUserIdAndLogDateBetweenOrderByLogDateDesc(
                user.getId(), start, end);
    }

    /**
     * Replaces an existing meal log.
     *
     * <p>Loads the stored record first so its owner can be compared against the
     * caller, then overwrites the id and owner on the incoming object before
     * saving, so neither can be set from the request body.
     *
     * @param username the authenticated caller
     * @param logId    id of the meal log to replace
     * @param updated  the replacement state
     * @return the saved meal log
     * @throws NotFoundException  if no meal log has that id
     * @throws ForbiddenException if the record belongs to another user
     */
    public MealLog updateMealLog(String username, Long logId, MealLog updated) {
        User user = getUser(username);
        MealLog existing = mealLogRepository.findById(logId)
                .orElseThrow(() -> new NotFoundException("Meal log not found."));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have access to this record.");
        }
        updated.setId(logId);
        updated.setUser(user);
        return mealLogRepository.save(updated);
    }

    /**
     * Deletes a meal log the caller owns.
     *
     * <p>The record is loaded before deletion purely so ownership can be
     * verified; deleting by id alone would let any authenticated user remove any
     * meal log in the system.
     *
     * @param username the authenticated caller
     * @param logId    id of the meal log to delete
     * @throws NotFoundException  if no meal log has that id
     * @throws ForbiddenException if the record belongs to another user
     */
    public void deleteMealLog(String username, Long logId) {
        User user = getUser(username);
        MealLog existing = mealLogRepository.findById(logId)
                .orElseThrow(() -> new NotFoundException("Meal log not found."));
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have access to this record.");
        }
        mealLogRepository.deleteById(logId);
    }
}
