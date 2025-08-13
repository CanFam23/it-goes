package com.it_goes.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.logging.Logger;

@RestController
public class HealthController {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    @GetMapping("health")
    public Map<String, Object> health(){
        logger.info("Health endpoint called");
        return Map.of("status", "UP",
                "time", Instant.now().toString(),
                "message", "OK");
    }
}
