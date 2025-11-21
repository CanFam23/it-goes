package com.it_goes.api.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping("/all")
    public Map<String, String> allAccess() {
        return Map.of("status", "UP",
                "time", Instant.now().toString(),
                "message", "All endpoint Called");
    }

    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public Map<String, String> userAccess() {
        return Map.of("status", "UP",
                "time", Instant.now().toString(),
                "message", "User endpoint Called");
    }
}
