package com.it_goes.api.jpa.repo;

import com.it_goes.api.jpa.model.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SeasonRepository extends JpaRepository<Season, Long> {

    @Query(value = """
            SELECT * from season
            WHERE EXTRACT(year FROM start_date) = :year
            """, nativeQuery=true)
    Optional<Season> findByStartYear(@Param("year") int year);
}
