package com.it_goes.api.service;

import com.it_goes.api.dto.TripDto;
import com.it_goes.api.dto.TripSummaryDto;
import com.it_goes.api.jpa.model.Trip;
import com.it_goes.api.jpa.model.User;
import org.springframework.data.domain.Page;

public interface TripService {
     static TripSummaryDto toTripSummaryDto(Trip trip) {
        return new TripSummaryDto(trip.getId(),trip.getTitle(),
                trip.getDescription(), trip.getLocation().getName(),
                trip.getLocation().getState().getName(),trip.getDateOfTrip());
    }

    static TripDto toTripDto(Trip trip) {
         return new TripDto(trip.getId(),trip.getTitle(),
                 trip.getDescription(), trip.getLocation().getName(),
                 trip.getLocation().getState().getName(),trip.getDateOfTrip(),
                 trip.getDatePosted(), trip.getDateUpdated(),
                 trip.getGpxContent(), trip.getAuthor().getUsername(), 
                 trip.getUsers().stream().map(User::getUsername).toArray(String[]::new));
    }

    TripDto getTrip(Long tripId);

     Page<TripSummaryDto> getRecentTripSummaries(int numTrips);
}
