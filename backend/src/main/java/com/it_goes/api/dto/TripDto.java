package com.it_goes.api.dto;

import org.locationtech.jts.geom.LineString;

import java.time.LocalDate;

public record TripDto(Long id, String title, String desc,
                      String locationName, String stateCountry,
                      LocalDate dateOfTrip, LocalDate datePosted, LocalDate dateUpdated,
                      LineString gpxContent, String authorUsername, String[] peopleInvolved) {
}
