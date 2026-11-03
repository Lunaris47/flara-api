package com.flara.repository;

import com.flara.entity.MedicationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data repository for medication log records.
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
public interface MedicationLogRepository extends JpaRepository<MedicationLog, Long> {
    /**
     * Every dose the user has logged, newest day first.
     *
     * @param userId the owning user's id
     * @return all medication logs for that user, ordered by log date descending
     */
    List<MedicationLog> findByUserIdOrderByLogDateDesc(Long userId);
    /**
     * Doses scheduled for one specific day.
     *
     * <p>A list rather than an {@code Optional}, since a user may take several
     * medications, or several doses of one, in a day.
     *
     * @param userId  the owning user's id
     * @param logDate the day to fetch
     * @return that day's doses, empty if none were recorded
     */
    List<MedicationLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);
    /**
     * Doses within an inclusive date range, newest first. Used for adherence
     * reporting over a window.
     *
     * @param userId the owning user's id
     * @param start  first day of the range, inclusive
     * @param end    last day of the range, inclusive
     * @return doses in the range, ordered by log date descending
     */
    List<MedicationLog> findByUserIdAndLogDateBetweenOrderByLogDateDesc(Long userId, LocalDate start, LocalDate end);
}