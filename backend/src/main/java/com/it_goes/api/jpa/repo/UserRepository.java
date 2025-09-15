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

    /**
     * Gets the number of days each user has skied between the start and end date
     * @param start Start date
     * @param end End date
     * @return List of {@link FirstNameDaysYear} projections with the numbers of days skied and the first name of the user, and the year found in the start
     */
    @Query(value = """
     SELECT u.first_name, COUNT(*) AS days_skied,
     :start AS startDate,
     EXTRACT(YEAR FROM CAST(:start AS DATE)) AS year
     FROM trip_users
     JOIN trip as t ON trip_users.trip_id = t.id
     JOIN it_goes_user as u ON trip_users.user_id = u.id
     WHERE t.date_of_trip >= :start and t.date_of_trip <= :end
     GROUP BY u.id,u.first_name;
    """, nativeQuery = true)
    List<FirstNameDaysYear> getDaysSkied(@Param("start") LocalDate start, @Param("end") LocalDate end);

    /**
     * Gets the number of days each user has skied for each season in the database
     * @return List of {@link FirstNameDaysYear} projections with the numbers of days skied, the first name of the user, and the year
     */
    @Query(value = """
     SELECT u.first_name, COUNT(*) AS days_skied, EXTRACT(year FROM s.start_date) as year
     FROM trip_users
     JOIN trip as t ON trip_users.trip_id = t.id
     JOIN it_goes_user as u ON trip_users.user_id = u.id
     JOIN season as s ON t.season_id = s.id
     GROUP BY u.id,u.first_name, s.start_date;
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
      SELECT u.first_name, l.name AS location, COUNT(*) AS days_skied,
      :start AS startDate,
      EXTRACT(YEAR FROM CAST(:start AS DATE)) AS year
      FROM trip_users
      JOIN trip AS t ON trip_users.trip_id = t.id
      JOIN it_goes_user AS u ON trip_users.user_id = u.id
      JOIN location AS l on t.location_id = l.id
      JOIN season as s ON t.season_id = s.id
      WHERE t.date_of_trip >= :start and t.date_of_trip <= :end
      GROUP BY u.id,u.first_name,l.name, s.start_date;
    """, nativeQuery = true)
    List<FirstNameDaysLocationYear> getDaysSkiedEachLocation(@Param("start") LocalDate start, @Param("end") LocalDate end);

    /**
     * Gets the number of days each user has skied between the start and end date at every location in the database all time
     * @return List of {@link FirstNameDaysLocationYear} projections with the numbers of days skied, the first name of the user,
     * the location of each trip, the year found in the start variable given
     */
    @Query(value = """
      SELECT u.first_name, l.name AS location, COUNT(*) AS days_skied,
      FROM trip_users
      JOIN trip AS t ON trip_users.trip_id = t.id
      JOIN it_goes_user AS u ON trip_users.user_id = u.id
      JOIN location AS l on t.location_id = l.id
      JOIN season as s ON t.season_id = s.id
      GROUP BY u.id,u.first_name,l.name, s.start_date;
    """, nativeQuery = true)
    List<FirstNameDaysLocationYear> getDaysSkiedEachLocation();

}
