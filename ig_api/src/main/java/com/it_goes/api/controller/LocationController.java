package com.it_goes.api.controller;

import com.it_goes.api.jpa.model.Location;
import com.it_goes.api.jpa.repo.LocationRepository;
import com.it_goes.api.util.enums.Country;
import com.it_goes.api.util.enums.State;
import com.it_goes.api.util.exception.AccountRequestException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/locations")
public class LocationController {
    private final LocationRepository locations;

    public LocationController(LocationRepository locations) {
        this.locations = locations;
    }

    @GetMapping
    public List<LocationResponse> list() {
        return locations.findAll().stream().map(LocationResponse::of)
                .sorted(Comparator.comparing(LocationResponse::name)).toList();
    }

    @PostMapping
    public ResponseEntity<LocationResponse> create(@Valid @RequestBody LocationRequest request) {
        String name = request.name().strip();
        if (name.isEmpty() || name.length() > 255) {
            throw new AccountRequestException(HttpStatus.BAD_REQUEST, "Enter a location name up to 255 characters.");
        }
        if (request.country() == Country.US && request.state() == null) {
            throw new AccountRequestException(HttpStatus.BAD_REQUEST, "Choose a state for a US location.");
        }
        if (locations.existsByNameIgnoreCaseAndStateAndCountry(name, request.state(), request.country())) {
            throw new AccountRequestException(HttpStatus.CONFLICT, "That location already exists.");
        }
        Location saved = locations.save(new Location(name, request.state(), request.country()));
        return ResponseEntity.status(201).body(LocationResponse.of(saved));
    }

    @GetMapping("/options")
    public LocationOptions options() {
        return new LocationOptions(Arrays.stream(State.values()).map(value -> new Option(value.name(), value.getName())).toList(),
                Arrays.stream(Country.values()).map(value -> new Option(value.name(), value.getName())).toList());
    }

    public record LocationRequest(@NotBlank String name, State state, @NotNull Country country) {}
    public record LocationResponse(Long id, String name, String state, String country) {
        static LocationResponse of(Location location) {
            return new LocationResponse(location.getId(), location.getName(),
                    location.getState() == null ? null : location.getState().name(), location.getCountry().name());
        }
    }
    public record Option(String value, String label) {}
    public record LocationOptions(List<Option> states, List<Option> countries) {}
}
