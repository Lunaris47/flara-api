package com.flara.repository;

import com.flara.entity.Flare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data repository for flare episodes.
 *
 * <p>No implementation is written for this interface. Spring Data generates one
 * at startup, supplying {@code save}, {@code findById}, {@code deleteById} and
 * the rest from {@link JpaRepository}, and deriving a query for each method
 * below by parsing its name. A misspelled field name fails at startup rather
 * than at runtime.
 *
 * <p>Both derived queries are scoped by {@code userId}, so neither can return
 * another user's flares. Note that {@code findById}, inherited from
 * {@link JpaRepository}, is <em>not</em> scoped that way: it looks up any flare
 * by primary key. That is why {@code FlareService} compares the loaded flare's
 * owner against the caller before allowing an update or a delete.
 */
@Repository
public interface FlareRepository extends JpaRepository<Flare, Long> {

    /**
     * Every flare the user has recorded, most recent first.
     *
     * @param userId the owning user's id
     * @return that user's flares, ordered by start date descending
     */
    List<Flare> findByUserIdOrderByStartDateDesc(Long userId);

    /**
     * Flares that began within an inclusive window, most recent first.
     *
     * <p>Takes {@link LocalDateTime} rather than {@code LocalDate} because a
     * flare is timestamped to the hour, not the day. Callers working in whole
     * days pass the start of the first day and the end of the last.
     *
     * @param userId the owning user's id
     * @param start  beginning of the window, inclusive
     * @param end    end of the window, inclusive
     * @return flares starting in the window, ordered by start date descending
     */
    List<Flare> findByUserIdAndStartDateBetweenOrderByStartDateDesc(Long userId, LocalDateTime start, LocalDateTime end);
}
