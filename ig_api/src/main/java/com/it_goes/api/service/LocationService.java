package com.it_goes.api.service;

import com.it_goes.api.jpa.model.Location;
import com.it_goes.api.jpa.repo.LocationRepository;
import com.it_goes.api.util.enums.Country;
import com.it_goes.api.util.enums.State;
import com.it_goes.api.util.exception.AccountRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocationService {
    private final LocationRepository locations;

    public LocationService(LocationRepository locations) {
        this.locations = locations;
    }

    @Transactional
    public Location create(String rawName, Country country, State state) {
        String name = rawName == null ? "" : rawName.strip();
        if (name.isEmpty() || name.length() > 255 || country == null) {
            throw new AccountRequestException(HttpStatus.BAD_REQUEST, "Enter a location name and country.");
        }
        if (country == Country.US && state == null) {
            throw new AccountRequestException(HttpStatus.BAD_REQUEST, "Choose a state for a US location.");
        }
        if (locations.existsByNameIgnoreCaseAndStateAndCountry(name, state, country)) {
            throw new AccountRequestException(HttpStatus.CONFLICT, "That location already exists.");
        }
        return locations.save(new Location(name, state, country));
    }
}
