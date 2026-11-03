package com.flara.repository;

import com.flara.entity.MealLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data repository for meal log records.
 *
 * <p>No implementation is written for this interface. At startup Spring Data
 * generates one, supplying {@code save}, {@code findById}, {@code findAll},
 * {@code deleteById} and the rest from {@link JpaRepository}, and deriving a
 * query for each method declared below by parsing its name.
 *
 * <p>Every derived query is scoped by {@code userId}. Nothing here can return
 * another user's rows, which is the first of the two layers protecting a
 * record: this one narrows the query, and the service layer verifies ownership
 * before any update or delete.
 */
@Repository
public interface MealLogRepository extends JpaRepository<MealLog, Long> {
    /**
     * Every meal the user has logged, newest day first.
     *
     * @param userId the owning user's id
     * @return all meals for that user, ordered by log date descending
     */
    List<MealLog> findByUserIdOrderByLogDateDesc(Long userId);
    /**
     * Meals logged on one specific day.
     *
     * <p>Returns a list rather than an {@code Optional} because a user records
     * several meals per day.
     *
     * @param userId  the owning user's id
     * @param logDate the day to fetch
     * @return that day's meals, empty if none were recorded
     */
    List<MealLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);
    /**
     * Meals within an inclusive date range, newest first. Used when building a
     * report over a window rather than a single day.
     *
     * @param userId the owning user's id
     * @param start  first day of the range, inclusive
     * @param end    last day of the range, inclusive
     * @return meals in the range, ordered by log date descending
     */
    List<MealLog> findByUserIdAndLogDateBetweenOrderByLogDateDesc(Long userId, LocalDate start, LocalDate end);
}