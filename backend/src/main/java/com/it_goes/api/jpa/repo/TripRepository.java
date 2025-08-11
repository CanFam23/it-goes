package com.it_goes.api.jpa.repo;

import com.it_goes.api.jpa.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trip, Long> {
}
