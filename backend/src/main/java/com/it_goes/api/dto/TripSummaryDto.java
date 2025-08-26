package com.it_goes.api.dto;

import java.time.LocalDate;

public record TripSummaryDto(Long id, String title, String desc, String locationName, String stateCountry, LocalDate dateOfTrip, String imageURL) {
}
