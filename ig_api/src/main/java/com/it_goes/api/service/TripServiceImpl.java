package com.it_goes.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.it_goes.api.dto.TripDto;
import com.it_goes.api.dto.CreateTripRequest;
import com.it_goes.api.dto.TripSummaryDto;
import com.it_goes.api.jpa.model.Trip;
import com.it_goes.api.jpa.model.User;
import com.it_goes.api.jpa.model.Location;
import com.it_goes.api.jpa.model.Season;
import com.it_goes.api.jpa.projection.GeoJsonDistanceElevation;
import com.it_goes.api.jpa.repo.TripRepository;
import com.it_goes.api.jpa.repo.UserRepository;
import com.it_goes.api.jpa.repo.LocationRepository;
import com.it_goes.api.jpa.repo.SeasonRepository;
import com.it_goes.api.util.exception.AccountRequestException;
import com.it_goes.api.util.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TripServiceImpl implements TripService{

    private final Logger logger = LoggerFactory.getLogger(TripServiceImpl.class);

    private final TripRepository tripRepo;
    private final UserRepository userRepo;
    private final LocationRepository locationRepo;
    private final LocationService locationService;
    private final SeasonRepository seasonRepo;

    public TripServiceImpl(TripRepository tripRepo, UserRepository userRepo,
                           LocationRepository locationRepo, SeasonRepository seasonRepo,
                           LocationService locationService) {
        this.tripRepo = tripRepo;
        this.userRepo = userRepo;
        this.locationRepo = locationRepo;
        this.locationService = locationService;
        this.seasonRepo = seasonRepo;
    }

    @Override
    @Transactional
    public TripDto createTrip(String username, CreateTripRequest request) {
        String title = request.title().strip();
        if (title.isEmpty() || title.length() > 255 || request.dateOfTrip().isAfter(LocalDate.now())) {
            throw new AccountRequestException(HttpStatus.BAD_REQUEST, "Enter a title and past or current date.");
        }
        if ((request.locationId() == null) == (request.newLocation() == null)
                || request.locationId() != null && request.locationId() < 1) {
            throw new AccountRequestException(HttpStatus.BAD_REQUEST, "Choose an existing location or add a new one.");
        }
        String description = TripBody.validateAndSummarize(request.body());
        User author = userRepo.findByUsername(username).orElseThrow(() -> new NotFoundException("User not found."));
        Location location = request.newLocation() == null
                ? locationRepo.findById(request.locationId())
                    .orElseThrow(() -> new AccountRequestException(HttpStatus.BAD_REQUEST, "Choose an existing location."))
                : locationService.create(request.newLocation().name(), request.newLocation().country(),
                    request.newLocation().state());
        List<Long> participantIds = request.participantIds() == null ? List.of() : request.participantIds();
        if (participantIds.size() > 100 || participantIds.stream().anyMatch(id -> id == null || id < 1)
                || new HashSet<>(participantIds).size() != participantIds.size()
                || participantIds.contains(author.getId())) {
            throw new AccountRequestException(HttpStatus.BAD_REQUEST, "Choose valid participants other than yourself.");
        }
        List<User> participants = userRepo.findAllById(participantIds);
        if (participants.size() != participantIds.size()) {
            throw new AccountRequestException(HttpStatus.BAD_REQUEST, "One or more participants were not found.");
        }
        int seasonYear = request.dateOfTrip().getMonthValue() >= 10
                ? request.dateOfTrip().getYear() : request.dateOfTrip().getYear() - 1;
        Season season = seasonRepo.findByStartYear(seasonYear).orElseGet(() -> seasonRepo.save(new Season(seasonYear)));
        Trip trip = new Trip(title, author, location, request.dateOfTrip());
        trip.setDescription(description);
        trip.setBodyJson(request.body());
        trip.setSeason(season);
        participants.forEach(trip::addUser);
        LineString route = parseRoute(request.route());
        if (route != null) trip.setRoute(route);
        Trip saved = tripRepo.saveAndFlush(trip);
        return TripService.toTripDto(saved, request.route());
    }

    private LineString parseRoute(JsonNode route) {
        if (route == null || route.isNull()) return null;
        if (!"LineString".equals(route.path("type").asText()) || !route.path("coordinates").isArray()
                || route.path("coordinates").size() < 2 || route.path("coordinates").size() > 5000) {
            throw new AccountRequestException(HttpStatus.BAD_REQUEST, "Choose a valid GPX route.");
        }
        List<Coordinate> points = new ArrayList<>();
        for (JsonNode point : route.path("coordinates")) {
            if (!point.isArray() || point.size() != 2 || !point.get(0).isNumber() || !point.get(1).isNumber()
                    || Math.abs(point.get(0).asDouble()) > 180 || Math.abs(point.get(1).asDouble()) > 90) {
                throw new AccountRequestException(HttpStatus.BAD_REQUEST, "Choose a valid GPX route.");
            }
            points.add(new Coordinate(point.get(0).asDouble(), point.get(1).asDouble()));
        }
        LineString line = TripService.gf.createLineString(points.toArray(Coordinate[]::new));
        line.setSRID(4326);
        return line;
    }

    /**{@inheritDoc}*/
    @Override
    @Transactional(readOnly = true)
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
            throw new NotFoundException("Trip with ID #" + tripId + " not found");
        }

        // If we use the store route column in the trip table, a linestring is returned
        // but each point only contains a x and y coordinate
        // so, this fetches the route so the z and m (time) measurements are included.
        final String route = tripRepo.getTripRoute(tripId).orElse("");
        final ObjectMapper mapper = new ObjectMapper();

        // Try to parse the string route into a json node.
        try {
            final JsonNode routeJson = route.isBlank() ? null : mapper.readTree(route);

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
            throw new IllegalArgumentException("Page number (" + pageNum + ") must be zero or greater");
        }

        final Pageable pageable = PageRequest.of(pageNum, pageSize);

        final Page<Trip> tripsFound = tripRepo.findAllByOrderByDateOfTripDesc(pageable);

        logger.info("getTripSummaries: Found {} recent trips", tripsFound.getNumberOfElements());

        return tripsFound.map(TripService::toTripSummaryDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonNode getAllTripFeatureRoutes() {
        final List<Trip> trips = tripRepo.findAll();

        // Get route data for each trip
        final Set<GeoJsonDistanceElevation> routeStrings = tripRepo.getAllTripRoutes();

        final JsonMapper mapper = new JsonMapper();

        logger.info("getAllTripFeatureRoutes: Found {} routes for {} trips", routeStrings.size(), trips.size());

        // Convert the geojson data found into JSON from string, create a new RouteData helper class instance with found data
        // Then add the obj to a hashmap with trip id -> obj for fast access to it
        final Map<Long,RouteData> routeNodes = routeStrings.stream()
                    .map(s -> {
                        try {
                            return new RouteData(mapper.readTree(s.getGeojson()), s.getLengthMi(), s.getElevationFt());
                        } catch (IOException e) {
                            throw new IllegalStateException("Invalid stored trip route", e);
                        }
                    })
                    .collect(Collectors.toMap(rd-> rd.route.get("id").asLong(),
                            rd -> rd));

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
