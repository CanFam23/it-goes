package com.it_goes.api.jpa.model;

import java.time.LocalDate;
import java.util.*;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "season")
public class Season {

    public Season(LocalDate start, LocalDate end) {
        this.startDate = start;
        this.endDate = end;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false)
    private LocalDate startDate;

    @Setter
    @Column(nullable = false)
    private LocalDate endDate;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "season",cascade = CascadeType.ALL)
    private final Set<Trip> trips = new HashSet<>();

    public Set<Trip> getTrips() {
        return Collections.unmodifiableSet(trips);
    }

    public boolean addTrip(Trip trip) {
        return trips.add(trip);
    }

    public boolean removeTrip(Trip trip) {
        return trips.remove(trip);
    }
}
