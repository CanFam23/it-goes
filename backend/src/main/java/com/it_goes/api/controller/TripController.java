package com.it_goes.api.controller;

import com.it_goes.api.dto.TripSummaryDto;
import com.it_goes.api.service.TripService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class TripController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping("getTrips")
    public ResponseEntity<Map<String,Object>> getTrips(
            @RequestParam(defaultValue = "3",name = "pageSize") int pageSize,
            @RequestParam(defaultValue = "0",name = "pageNum") int pageNum
    ){
        final Page<TripSummaryDto> recentTrips = tripService.getTripSummaries(pageSize, pageNum);

        final List<TripSummaryDto> recentTripsList = recentTrips.getContent();

        logger.info("getTrips: (pageSize: {}, pageNum: {}) Found {} recent trips.", pageSize, pageNum, recentTripsList.size());

        final Map<String, Object> tripData = new HashMap<>();

        tripData.put("trips", recentTripsList);
        tripData.put("numPages", recentTrips.getTotalPages());

        return ResponseEntity.ok(tripData);
    }
}
