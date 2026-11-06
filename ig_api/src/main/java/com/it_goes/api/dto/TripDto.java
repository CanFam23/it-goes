package com.it_goes.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import org.locationtech.jts.geom.Point;

import java.time.LocalDate;

public record TripDto(Long id, String title, String desc,
                      String locationName, String stateCountry, 
                      LocalDate dateOfTrip, LocalDate datePosted, LocalDate dateUpdated,
                      String authorUsername, String[] peopleInvolved, JsonNode route, double[] location,
                      String imageURL, String authorName, String[] companionNames, JsonNode body) {
    public TripDto(Long id, String title, String desc, String locationName, String stateCountry,
                   LocalDate dateOfTrip, LocalDate datePosted, LocalDate dateUpdated,
                   String authorUsername, String[] peopleInvolved, JsonNode route, double[] location,
                   String imageURL, String authorName, String[] companionNames) {
        this(id, title, desc, locationName, stateCountry, dateOfTrip, datePosted, dateUpdated,
                authorUsername, peopleInvolved, route, location, imageURL, authorName, companionNames, null);
    }
}
