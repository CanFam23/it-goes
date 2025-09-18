package com.it_goes.api.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;

public record TripDto(Long id, String title, String desc,
                      String locationName, String stateCountry,
                      LocalDate dateOfTrip, LocalDate datePosted, LocalDate dateUpdated,
                      JsonNode route, String authorUsername, String[] peopleInvolved) {
}
