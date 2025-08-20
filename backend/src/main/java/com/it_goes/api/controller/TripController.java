package com.it_goes.api.controller;

import com.it_goes.api.dto.TripSummaryDto;
import com.it_goes.api.service.TripService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("getRecentTrips")
    public ResponseEntity<List<TripSummaryDto>> getRecentTrips(
            @RequestParam(defaultValue = "3",name = "numTrips") int numTrips,
            @RequestParam(defaultValue = "0",name = "pageNum") int pageNum
    ){

        final Page<TripSummaryDto> recentTrips = tripService.getRecentTripSummaries(pageNum, numTrips);

        final List<TripSummaryDto> recentTripsList = recentTrips.getContent();

        logger.info("getRecentTrips: (numTrips: {}) Found {} recent trips.", numTrips, recentTripsList.size());

        return ResponseEntity.ok(recentTripsList);
    }
}
