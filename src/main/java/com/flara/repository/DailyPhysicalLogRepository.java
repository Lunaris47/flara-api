package com.flara.repository;

import com.flara.entity.DailyPhysicalLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for daily physical symptom logs.
 *
 * <p>No implementation is written for this interface. Spring Data generates one
 * at startup from {@link JpaRepository} and derives a query for each method
 * below by parsing its name.
 *
 * <p>Every derived query is scoped by {@code userId}, so none of them can return
 * another user's rows. The inherited {@code findById} is not scoped that way,
 * which is why {@code DailyLogService} checks ownership before any update or
 * delete.
 */
@Repository
public interface DailyPhysicalLogRepository extends JpaRepository<DailyPhysicalLog, Long> {

    /**
     * Every physical log the user has recorded, newest first.
     *
     * @param userId the owning user's id
     * @return all physical logs for that user, ordered by log date descending
     */
    List<DailyPhysicalLog> findByUserIdOrderByLogDateDesc(Long userId);

    /**
     * The physical log for one specific day, if one exists.
     *
     * <p>Returns an {@code Optional} rather than a list because there is at most
     * one physical log per user per day. An empty result means the user did not
     * log that day, which the controller translates into a 404 rather than
     * treating as an error.
     *
     * @param userId  the owning user's id
     * @param logDate the day to fetch
     * @return the log for that day, or empty if none was recorded
     */
    Optional<DailyPhysicalLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);

    /**
     * Physical logs within an inclusive date range, newest first.
     *
     * <p>Used by the PDF report, and pairs with the equivalent method on
     * {@link DailyMentalLogRepository}: pulling both over the same window is
     * what lets physical and mental state be compared day by day.
     *
     * @param userId the owning user's id
     * @param start  first day of the range, inclusive
     * @param end    last day of the range, inclusive
     * @return logs in the range, ordered by log date descending
     */
    List<DailyPhysicalLog> findByUserIdAndLogDateBetweenOrderByLogDateDesc(Long userId, LocalDate start, LocalDate end);
}
