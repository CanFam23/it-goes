package com.it_goes.api.jpa.repo;

import com.it_goes.api.jpa.projection.FirstNameDaysLocationYear;
import com.it_goes.api.jpa.projection.FirstNameDaysYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.it_goes.api.jpa.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    void deleteUserByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /**
     * Gets the number of days each user has skied between the start and end date
     * @param start Start date
     * @param end End date
     * @return List of {@link FirstNameDaysYear} projections with the numbers of days skied and the first name of the user, and the year found in the start
     */
    @Query(value = """
     WITH trip_people AS (
       SELECT trip_id, user_id FROM trip_users
       UNION
       SELECT id AS trip_id, author_id AS user_id FROM trip WHERE author_id IS NOT NULL
     )
     , ski_days AS (
       SELECT tp.user_id, t.date_of_trip AS ski_date FROM trip_people tp JOIN trip t ON t.id = tp.trip_id
       UNION
       SELECT user_id, ski_date FROM ski_day_log
     )
     SELECT u.first_name, COUNT(*) AS days_skied,
     :start AS startDate,
     EXTRACT(YEAR FROM CAST(:start AS DATE)) AS year
     FROM ski_days AS d
     JOIN it_goes_user AS u ON d.user_id = u.id
     WHERE d.ski_date >= :start and d.ski_date <= :end
     GROUP BY u.id,u.first_name;
    """, nativeQuery = true)
    List<FirstNameDaysYear> getDaysSkied(@Param("start") LocalDate start, @Param("end") LocalDate end);

    /**
     * Gets the number of days each user has skied for each season in the database
     * @return List of {@link FirstNameDaysYear} projections with the numbers of days skied, the first name of the user, and the year
     */
    @Query(value = """
     WITH trip_people AS (
       SELECT trip_id, user_id FROM trip_users
       UNION
       SELECT id AS trip_id, author_id AS user_id FROM trip WHERE author_id IS NOT NULL
     )
     , ski_days AS (
       SELECT tp.user_id, t.date_of_trip AS ski_date FROM trip_people tp JOIN trip t ON t.id = tp.trip_id
       UNION
       SELECT user_id, ski_date FROM ski_day_log
     )
     SELECT u.first_name, COUNT(*) AS days_skied,
            EXTRACT(YEAR FROM d.ski_date - INTERVAL '9 months') AS year
     FROM ski_days d JOIN it_goes_user u ON d.user_id = u.id
     GROUP BY u.id, u.first_name, EXTRACT(YEAR FROM d.ski_date - INTERVAL '9 months');
    """, nativeQuery = true)
    List<FirstNameDaysYear> getDaysSkied();

    /**
     * Gets the number of days each user has skied between the start and end date at every location in the database
     * @param start Start date
     * @param end End date
     * @return List of {@link FirstNameDaysLocationYear} projections with the numbers of days skied, the first name of the user,
     * the location of each trip, the year found in the start variable given
     */
    @Query(value = """
      WITH trip_people AS (
        SELECT trip_id, user_id FROM trip_users
        UNION
        SELECT id AS trip_id, author_id AS user_id FROM trip WHERE author_id IS NOT NULL
      )
      , ski_places AS (
        SELECT tp.user_id, t.date_of_trip AS ski_date, t.location_id FROM trip_people tp JOIN trip t ON t.id = tp.trip_id
        WHERE t.location_id IS NOT NULL
        UNION
        SELECT user_id, ski_date, location_id FROM ski_day_log
      )
      SELECT u.first_name, l.name AS location, COUNT(*) AS days_skied,
      :start AS startDate,
      EXTRACT(YEAR FROM CAST(:start AS DATE)) AS year
      FROM ski_places AS p
      JOIN it_goes_user AS u ON p.user_id = u.id
      JOIN location AS l on p.location_id = l.id
      WHERE p.ski_date >= :start and p.ski_date <= :end
      GROUP BY u.id,u.first_name,l.id,l.name;
    """, nativeQuery = true)
    List<FirstNameDaysLocationYear> getDaysSkiedEachLocation(@Param("start") LocalDate start, @Param("end") LocalDate end);

    /**
     * Gets the number of days each user has skied between the start and end date at every location in the database all time
     * @return List of {@link FirstNameDaysLocationYear} projections with the numbers of days skied, the first name of the user,
     * the location of each trip, the year found in the start variable given
     */
    @Query(value = """
      WITH trip_people AS (
        SELECT trip_id, user_id FROM trip_users
        UNION
        SELECT id AS trip_id, author_id AS user_id FROM trip WHERE author_id IS NOT NULL
      )
      , ski_places AS (
        SELECT tp.user_id, t.date_of_trip AS ski_date, t.location_id FROM trip_people tp JOIN trip t ON t.id = tp.trip_id
        WHERE t.location_id IS NOT NULL
        UNION
        SELECT user_id, ski_date, location_id FROM ski_day_log
      )
      SELECT u.first_name, l.name AS location, COUNT(*) AS days_skied,
             EXTRACT(YEAR FROM p.ski_date - INTERVAL '9 months') AS year
      FROM ski_places AS p
      JOIN it_goes_user AS u ON p.user_id = u.id
      JOIN location AS l on p.location_id = l.id
      GROUP BY u.id,u.first_name,l.id,l.name, EXTRACT(YEAR FROM p.ski_date - INTERVAL '9 months');
    """, nativeQuery = true)
    List<FirstNameDaysLocationYear> getDaysSkiedEachLocation();

}
