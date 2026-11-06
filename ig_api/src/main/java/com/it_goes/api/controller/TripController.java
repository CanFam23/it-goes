package com.it_goes.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.it_goes.api.dto.TripDto;
import com.it_goes.api.dto.CreateTripRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import com.it_goes.api.dto.TripSummaryDto;
import com.it_goes.api.dto.TripPageDto;
import com.it_goes.api.service.TripService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class TripController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping("trips")
    public ResponseEntity<TripDto> createTrip(Authentication authentication,
                                               @Valid @RequestBody CreateTripRequest request) {
        TripDto trip = tripService.createTrip(authentication.getName(), request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/trips/{id}").buildAndExpand(trip.id()).toUri();
        return ResponseEntity.created(location).body(trip);
    }

    @GetMapping("getTrips")
    public ResponseEntity<TripPageDto> getTrips(
            @RequestParam(defaultValue = "3",name = "pageSize") int pageSize,
            @RequestParam(defaultValue = "0",name = "pageNum") int pageNum
    ){
        if (pageSize < TripService.MIN_NUM_TRIPS || pageSize > TripService.MAX_NUM_TRIPS || pageNum < 0) {
            throw new IllegalArgumentException("Invalid trip page or size.");
        }
        final Page<TripSummaryDto> recentTrips = tripService.getTripSummaries(pageSize, pageNum);

        final List<TripSummaryDto> recentTripsList = recentTrips.getContent();

        logger.info("getTrips: (pageSize: {}, pageNum: {}) Found {} recent trips.", pageSize, pageNum, recentTripsList.size());

        return ResponseEntity.ok(new TripPageDto(recentTripsList, recentTrips.getTotalPages()));
    }

    @GetMapping("trips")
    public ResponseEntity<TripPageDto> getTripsCanonical(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return getTrips(size, page);
    }

    @GetMapping("getTrip")
    public ResponseEntity<TripDto> getTrip(
        @RequestParam(name="id") long id
    ){
        final TripDto trip = tripService.getTrip(id);

        logger.info("getTrip: Found trip with (id: {})", id);

        return ResponseEntity.ok(trip);
    }

    @GetMapping("trips/{id}")
    public ResponseEntity<TripDto> getTripCanonical(@PathVariable long id) {
        return getTrip(id);
    }

    @GetMapping("getAllTripFeatureRoutes")
    public ResponseEntity<JsonNode> getAllTripRoutesFeatures(){
        logger.info("getAllTripRoutesFeatures: Getting all trip routes and features");

        return ResponseEntity.ok(tripService.getAllTripFeatureRoutes());
    }

    @GetMapping("trips/routes")
    public ResponseEntity<JsonNode> getAllTripRoutesFeaturesCanonical() {
        return getAllTripRoutesFeatures();
    }

}
