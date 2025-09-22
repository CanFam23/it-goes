package com.it_goes.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import org.locationtech.jts.geom.Point;

import java.time.LocalDate;

public record TripDto(Long id, String title, String desc,
                      String locationName, String stateCountry, 
                      LocalDate dateOfTrip, LocalDate datePosted, LocalDate dateUpdated,
                      String authorUsername, String[] peopleInvolved, JsonNode route, double[] location) {
}
