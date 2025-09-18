package com.it_goes.api.service;

import com.it_goes.api.dto.TripDto;
import com.it_goes.api.dto.TripSummaryDto;
import com.it_goes.api.jpa.model.Trip;
import com.it_goes.api.jpa.model.User;
import com.it_goes.api.util.exception.NotFoundException;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import org.locationtech.jts.geom.*;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.nio.file.Path;
import java.util.NoSuchElementException;

public interface TripService {
    int MIN_NUM_TRIPS = 1;
    int MAX_NUM_TRIPS = 5;

    GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);

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
                trip.getRoute(), trip.getAuthor().getUsername(),
                trip.getUsers().stream().map(User::getUsername).toArray(String[]::new));
    }

    /**
     * Given a {@link Path} of a .gpx file, attempts to convert the gpx file into a {@link LineString} object.
     * @param p Path of gpx file.
     * @return The created LineString object, or null if an error occurs or the file is not a gpx file.
     */
    static LineString toLineString(Path p){
        // Only convert gpx files to lineString
        if (!p.getFileName().toString().endsWith(".gpx")){
            return null;
        }

        try {
            final GPX gpx = GPX.read(p);
            final Track track = gpx.getTracks().getFirst();

            final TrackSegment segment = track.getSegments().getFirst();

            final Coordinate[] coords = segment.points()
                    .map(point -> {
                        if (point.getElevation().isEmpty() || point.getTime().isEmpty()) {
                            return null;
                        }

                        return new CoordinateXYZM(
                                point.getLongitude().doubleValue(),   // X
                                point.getLatitude().doubleValue(),    // Y
                                point.getElevation().get().doubleValue(), // Z
                                point.getTime().get().getEpochSecond()); // M / Time
                    })
                    .toArray(Coordinate[]::new);

            return gf.createLineString(coords);
        } catch (IOException | NullPointerException | NoSuchElementException e) {
            return null;
        }
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
     * @param pageSize Number of trips to get.
     * @return Page object containing the trip summary dtos.
     */
    Page<TripSummaryDto> getTripSummaries(int pageSize, int pageNum);
}
