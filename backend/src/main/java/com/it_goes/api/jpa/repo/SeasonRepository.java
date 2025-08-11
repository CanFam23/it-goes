package com.it_goes.api.jpa.repo;

import com.it_goes.api.jpa.model.Season;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeasonRepository extends JpaRepository<Season, Long> {
}
