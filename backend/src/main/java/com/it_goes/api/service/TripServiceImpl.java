package com.it_goes.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.it_goes.api.dto.TripDto;
import com.it_goes.api.dto.TripSummaryDto;
import com.it_goes.api.jpa.model.Trip;
import com.it_goes.api.jpa.projection.GeoJsonDistanceElevation;
import com.it_goes.api.jpa.repo.TripRepository;
import com.it_goes.api.util.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TripServiceImpl implements TripService{

    private final Logger logger = LoggerFactory.getLogger(TripServiceImpl.class);

    private final TripRepository tripRepo;

    public TripServiceImpl(TripRepository tripRepo) {
        this.tripRepo = tripRepo;
    }

    /**{@inheritDoc}*/
    @Override
    public TripDto getTrip(Long tripId) {
        if (tripId == null) {
            logger.warn("getTrip: tripId is null");
            throw new IllegalArgumentException("Trip id cannot be null");
        }

        if (tripId <= 0) {
            logger.warn("getTrip: tripId {} <= 0", tripId);
            throw new IllegalArgumentException("Trip id must be greater than zero");
        }

        final Trip tripFound = tripRepo.findById(tripId).orElse(null);

        if (tripFound == null) {
            logger.info("getTrip: Trip with ID {} not found", tripId);
            throw new NotFoundException("Trip with ID #" + tripId + " found");
        }

        // If we use the store route column in the trip table, a linestring is returned
        // but each point only contains a x and y coordinate
        // so, this fetches the route so the z and m (time) measurements are included.
        final String route = tripRepo.getTripRoute(tripId).orElse("");
        final ObjectMapper mapper = new ObjectMapper();

        // Try to parse the string route into a json node.
        try {
            final JsonNode routeJson = mapper.readTree(route);

            logger.info("getTrip: Trip with ID {} found", tripId);
            return TripService.toTripDto(tripFound, routeJson);
        } catch (JsonProcessingException e) {
            logger.warn("getTrip: Error reading");
            throw new RuntimeException(e); //TODO: Change? Probably should handle error instead of throwing a runtime ex
        }
    }

    /**
     * {@inheritDoc}
     * This method needs a `transactional` annotation because the {@link TripSummaryDto} object loads a LOB
     * object (The trips description). Reading a large object in Postgres needs a transactional action, they are not allowed
     * to be used in auto-commit mode.
     * */
    @Override
    @Transactional
    public Page<TripSummaryDto> getTripSummaries(int pageSize, int pageNum) {
        if (pageSize < TripService.MIN_NUM_TRIPS || pageSize > TripService.MAX_NUM_TRIPS) {
            throw new IllegalArgumentException("Number of trips (" + pageSize + ") is out of range");
        }

        if (pageNum < 0) {
            throw new IllegalArgumentException("Page number (" + pageNum + ") must be greater than zero");
        }

        final Pageable pageable = PageRequest.of(pageNum, pageSize);

        final Page<Trip> tripsFound = tripRepo.findAllByOrderByDateOfTripDesc(pageable);

        if (tripsFound == null || tripsFound.isEmpty()) {
            throw new NotFoundException("No recent trips found");
        }

        logger.info("getTripSummaries: Found {} recent trips", tripsFound.getNumberOfElements());

        return tripsFound.map(TripService::toTripSummaryDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonNode getAllTripFeatureRoutes() {
        final List<Trip> trips = tripRepo.findAll();

        if(trips.isEmpty()){
            throw new NotFoundException("No trips found in database!");
        }

        // Get route data for each trip
        final Set<GeoJsonDistanceElevation> routeStrings = tripRepo.getAllTripRoutes();

        final JsonMapper mapper = new JsonMapper();

        final Map<Long,RouteData> routeNodes;

        logger.info("getAllTripFeatureRoutes: Found {} routes for {} trips", routeStrings.size(), trips.size());

        // Convert the geojson data found into JSON from string, create a new RouteData helper class instance with found data
        // Then add the obj to a hashmap with trip id -> obj for fast access to it
        try {
             routeNodes = routeStrings.stream()
                    .map(s -> {
                        try {
                            return new RouteData(mapper.readTree(s.getGeojson()), s.getLengthMi(), s.getElevationFt());
                        } catch (IOException e) {
                            logger.warn("getAllTripFeaturesRoutes: Error reading json: {}", e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(rd-> rd.route.get("id").asLong(),
                            rd -> rd));
        } catch (Exception e) {
            logger.warn("getAllTripFeaturesRoutes: Error parsing json: {}", e.getMessage());
            return null;
        }

        // Root node, which is the json node that will be returned
        final ObjectNode rootNode = mapper.createObjectNode();

        // Specify type of data it holds
        rootNode.put("type","FeatureCollection");

        // Array to hold features
        final ArrayNode an = rootNode.putArray("features");

        // Make a new node for each trip found and add it to the array node above
        for (Trip t: trips) {
            final Long tripId = t.getId();
            final RouteData routeData = routeNodes.get(tripId);

            if (routeData == null){
                logger.warn("getAllTripFeatureRoutes: No route data found for trip #{}", tripId);
                continue; // Can't be displayed on a map if there is no route data...
            }

            final ObjectNode propertiesNode = mapper.createObjectNode();

            propertiesNode.put("id",tripId);
            propertiesNode.put("title", t.getTitle());
            propertiesNode.put("date", t.getDateOfTrip().toString());
            propertiesNode.put("locationName", t.getLocation().toString());

            propertiesNode.put("distance",routeData.distance);
            propertiesNode.put("elevation",routeData.elevation);

            final ObjectNode tripNode = mapper.createObjectNode();

            tripNode.put("type", "Feature");
            tripNode.set("properties", propertiesNode);
            tripNode.set("geometry", routeData.route);

            an.add(tripNode);

            break;
        }

        return rootNode;
    }

    /**
     * Helper class for holding data for a route
     */
    private static class RouteData {
        /** JsonNode containing geojson data of route */
        public JsonNode route;

        /** Distance of route */
        public double distance;

        /** Elevation gained in route */
        public double elevation;

        public RouteData(JsonNode route, double distance, double elevation){
            this.route = route;
            this.distance = distance;
            this.elevation = elevation;
        }
    }

}
