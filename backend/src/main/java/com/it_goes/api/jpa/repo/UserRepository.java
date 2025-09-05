package com.it_goes.api.jpa.repo;

import com.it_goes.api.jpa.projection.FirstNameDays;
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
     * @return List of {@link FirstNameDays} projections with the numbers of days skied and the first name of the user
     */
    @Query(value = """
     SELECT u.first_name, COUNT(*) AS days_skied FROM trip_users 
     JOIN trip as t ON trip_users.trip_id = t.id 
     JOIN it_goes_user as u ON trip_users.user_id = u.id 
     WHERE t.date_of_trip >= :start and t.date_of_trip <= :end
     GROUP BY u.id,u.first_name;
    """, nativeQuery = true)
    List<FirstNameDays> getDaysSkied(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
