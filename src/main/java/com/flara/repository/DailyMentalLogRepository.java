package com.flara.repository;

import com.flara.entity.DailyMentalLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for daily mental log records.
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
public interface DailyMentalLogRepository extends JpaRepository<DailyMentalLog, Long> {
    /**
     * Every mental log the user has recorded, newest first.
     *
     * @param userId the owning user's id
     * @return all mental logs for that user, ordered by log date descending
     */
    List<DailyMentalLog> findByUserIdOrderByLogDateDesc(Long userId);
    /**
     * The mental log for one specific day, if one exists.
     *
     * <p>Returns an {@code Optional} rather than a list because there is at most
     * one mental log per user per day. An empty result means the user did not
     * log that day, which is an ordinary state rather than an error.
     *
     * @param userId  the owning user's id
     * @param logDate the day to fetch
     * @return the log for that day, or empty if none was recorded
     */
    Optional<DailyMentalLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);
    /**
     * Mental logs within an inclusive date range, newest first.
     *
     * <p>Used by the PDF report, and pairs with the equivalent method on
     * {@link DailyPhysicalLogRepository}: fetching both over the same window is
     * what allows mental and physical state to be compared day by day.
     *
     * @param userId the owning user's id
     * @param start  first day of the range, inclusive
     * @param end    last day of the range, inclusive
     * @return logs in the range, ordered by log date descending
     */
    List<DailyMentalLog> findByUserIdAndLogDateBetweenOrderByLogDateDesc(Long userId, LocalDate start, LocalDate end);
}