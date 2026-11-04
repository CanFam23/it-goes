package com.it_goes.api.jpa.repo;

import com.it_goes.api.jpa.model.Location;
import com.it_goes.api.util.enums.Country;
import com.it_goes.api.util.enums.State;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
    boolean existsByNameIgnoreCaseAndStateAndCountry(String name, State state, Country country);
}
