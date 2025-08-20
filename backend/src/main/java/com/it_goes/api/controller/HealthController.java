package com.it_goes.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class HealthController {
    private final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @GetMapping("health")
    public Map<String, Object> health(){
        logger.info("Health endpoint called");
        return Map.of("status", "UP",
                "time", Instant.now().toString(),
                "message", "OK");
    }
}
