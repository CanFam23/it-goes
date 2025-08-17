package com.it_goes.api.dto;

import org.springframework.cglib.core.Local;

import java.time.LocalDate;

public record TripDto(Long id, String title, String desc,
                      String locationName, String stateCountry,
                      LocalDate dateOfTrip, LocalDate datePosted, LocalDate dateUpdated,
                      byte[] gpxContent, String authorUsername, String[] peopleInvolved) {
}
