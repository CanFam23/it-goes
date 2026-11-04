package com.it_goes.api.controller;

import com.it_goes.api.service.SkiDayService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/me/ski-days")
public class SkiDayController {
    private final SkiDayService service;

    public SkiDayController(SkiDayService service) {
        this.service = service;
    }

    @GetMapping
    public SkiDayService.DayList list(Authentication auth) {
        return service.list(auth.getName());
    }

    @PostMapping
    public ResponseEntity<SkiDayService.DayEntry> create(Authentication auth, @Valid @RequestBody DayRequest request) {
        return ResponseEntity.status(201).body(service.create(auth.getName(), request.date(), request.locationId()));
    }

    @PatchMapping("/{id}")
    public SkiDayService.DayEntry update(Authentication auth, @PathVariable Long id, @Valid @RequestBody DayRequest request) {
        return service.update(auth.getName(), id, request.date(), request.locationId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        service.delete(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    public record DayRequest(@NotNull LocalDate date, @NotNull Long locationId) {}
}
