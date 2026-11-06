package com.it_goes.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.it_goes.api.util.enums.Country;
import com.it_goes.api.util.enums.State;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CreateTripRequest(@NotBlank String title, @NotNull LocalDate dateOfTrip,
                                Long locationId, @Valid NewLocation newLocation, List<Long> participantIds,
                                @NotNull JsonNode body, JsonNode route) {
    public CreateTripRequest(String title, LocalDate dateOfTrip, Long locationId, List<Long> participantIds,
                             JsonNode body, JsonNode route) {
        this(title, dateOfTrip, locationId, null, participantIds, body, route);
    }

    public record NewLocation(@NotBlank String name, @NotNull Country country, State state) {}
}
