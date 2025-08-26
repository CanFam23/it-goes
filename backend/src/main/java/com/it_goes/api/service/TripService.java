package com.it_goes.api.service;

import com.it_goes.api.dto.TripDto;
import com.it_goes.api.dto.TripSummaryDto;
import com.it_goes.api.jpa.model.Trip;
import com.it_goes.api.jpa.model.User;
import com.it_goes.api.util.exception.NotFoundException;
import org.springframework.data.domain.Page;

public interface TripService {
    int MIN_NUM_TRIPS = 1;
    int MAX_NUM_TRIPS = 5;

    /**
     * Given a {@link Trip} object, converts it to a {@link TripSummaryDto}.
     * @param trip Trip Summary object to convert.
     * @return A new Trip Summary Dto object.
     */
    static TripSummaryDto toTripSummaryDto(Trip trip) {
        return new TripSummaryDto(trip.getId(), trip.getTitle(),
                trip.getDescription(), trip.getLocation().getName(),
                trip.getLocation().getState().getName(), trip.getDateOfTrip(), ImageService.buildImageUrl(trip.getCoverImageKey()));
    }

    /**
     * Given a {@link Trip} object, converts it to a {@link TripDto}.
     * @param trip Trip object to convert.
     * @return A new Trip Dto object.
     */
    static TripDto toTripDto(Trip trip) {
        return new TripDto(trip.getId(), trip.getTitle(),
                trip.getDescription(), trip.getLocation().getName(),
                trip.getLocation().getState().getName(), trip.getDateOfTrip(),
                trip.getDatePosted(), trip.getDateUpdated(),
                trip.getGpxContent(), trip.getAuthor().getUsername(),
                trip.getUsers().stream().map(User::getUsername).toArray(String[]::new));
    }

    /**
     * Gets the trip with the given ID. If no trip is found. Should throw an {@link IllegalArgumentException} if null is given
     * or an invalid id (<= 0) is given.
     * @param tripId ID of trip
     * @return A Trip DTO made from the found Trip
     * @throws NotFoundException If a trip with the given ID isn't found
     */
    TripDto getTrip(Long tripId);

    /**
     * Gets summaries of recent trips, should be able to query between {@link #MIN_NUM_TRIPS} and {@link #MIN_NUM_TRIPS} of trips.
     * @param pageNum Page number to get.
     * @param numTrips Number of trips to get.
     * @return Page object containing the trip summary dtos.
     */
    Page<TripSummaryDto> getRecentTripSummaries(int pageNum, int numTrips);
}
