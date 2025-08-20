package com.it_goes.api.service;

import com.it_goes.api.dto.TripDto;
import com.it_goes.api.dto.TripSummaryDto;
import com.it_goes.api.jpa.model.Trip;
import com.it_goes.api.jpa.repo.TripRepository;
import com.it_goes.api.util.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.logging.Logger;

@Service
public class TripServiceImpl implements TripService{

    private final Logger logger = Logger.getLogger(TripServiceImpl.class.getName());

    private final TripRepository tripRepo;

    public TripServiceImpl(TripRepository tripRepo) {
        this.tripRepo = tripRepo;
    }

    /**{@inheritDoc}*/
    @Override
    public TripDto getTrip(Long tripId) {
        if (tripId == null) {
            logger.warning("getTrip: tripId is null");
            throw new IllegalArgumentException("Trip id cannot be null");
        }

        if (tripId <= 0) {
            logger.warning("getTrip: tripId ("+ tripId + ") <= 0");
            throw new IllegalArgumentException("Trip id must be greater than zero");
        }

        final Trip tripFound = tripRepo.findById(tripId).orElse(null);

        if (tripFound == null) {
            logger.info("getTrip: Trip with ID ("+ tripId + ") not found");
            throw new NotFoundException("Trip with ID #" + tripId + " found");
        }

        logger.info("getTrip: Trip with ID ("+ tripId + ") found");
        return TripService.toTripDto(tripFound);
    }

    /**
     * {@inheritDoc}
     * This method needs a `transactional` annotation because the {@link TripSummaryDto} object loads a LOB
     * object (The trips description). Reading a large object in Postgres needs a transactional action, they are not allowed
     * to be used in auto-commit mode.
     * */
    @Override
    @Transactional
    public Page<TripSummaryDto> getRecentTripSummaries(int numTrips) {
        if (numTrips < TripService.MIN_NUM_TRIPS || numTrips > TripService.MAX_NUM_TRIPS) {
            throw new IllegalArgumentException("Number of trips (" + numTrips + ") is out of range");
        }

        final Pageable pageable = PageRequest.of(0, numTrips);

        final Page<Trip> tripsFound = tripRepo.findAllByOrderByDateOfTripDesc(pageable);

        if (tripsFound == null || tripsFound.isEmpty()) {
            throw new NotFoundException("No recent trips found");
        }

        return tripsFound.map(TripService::toTripSummaryDto);
    }
}
