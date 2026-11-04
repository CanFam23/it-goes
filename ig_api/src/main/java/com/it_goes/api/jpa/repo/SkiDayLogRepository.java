package com.it_goes.api.jpa.repo;

import com.it_goes.api.jpa.model.Location;
import com.it_goes.api.jpa.model.SkiDayLog;
import com.it_goes.api.jpa.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SkiDayLogRepository extends JpaRepository<SkiDayLog, Long> {
    List<SkiDayLog> findByUserOrderByDateDescIdDesc(User user);
    boolean existsByUserAndDateAndLocation(User user, LocalDate date, Location location);

    @Query(value = """
            SELECT COUNT(*) FROM (
                SELECT t.date_of_trip AS ski_date FROM trip t
                WHERE (t.author_id = :userId OR EXISTS (
                    SELECT 1 FROM trip_users tu WHERE tu.trip_id = t.id AND tu.user_id = :userId
                )) AND t.date_of_trip BETWEEN :start AND :end
                UNION
                SELECT l.ski_date FROM ski_day_log l
                WHERE l.user_id = :userId AND l.ski_date BETWEEN :start AND :end
            ) days
            """, nativeQuery = true)
    long countDistinctDays(@Param("userId") Long userId, @Param("start") LocalDate start,
                           @Param("end") LocalDate end);
}
