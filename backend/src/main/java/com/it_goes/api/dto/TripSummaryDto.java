package com.it_goes.api.dto;

import com.it_goes.api.jpa.model.Location;

import java.time.LocalDate;

public record TripSummaryDto(Long id, String title, String desc, String locationName, String stateCountry, LocalDate dateOfTrip) {
}
