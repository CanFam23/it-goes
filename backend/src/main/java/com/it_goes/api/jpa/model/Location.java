package com.it_goes.api.jpa.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.it_goes.api.util.enums.Country;
import com.it_goes.api.util.enums.State;
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
@Table(name = "location")
public class Location {

    public Location(String name, State state, Country country) {
        this.name = name;
        this.state = state;
        this.country = country;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @Column(nullable = false)
    private State state;

    @Setter
    @Column(nullable = false)
    private Country country;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "location",cascade = CascadeType.ALL)
    private final Set<Trip> trips = new HashSet<>();

    public Set<Trip> getTrips(){
        return Collections.unmodifiableSet(trips);
    }

    public boolean addTrip(Trip trip) {
        if (trip == null) return false;

        if (trips.add(trip)){
            trip.setLocation(this);
            return true;
        }

        return false;
    }

    public boolean removeTrip(Trip trip) {
        if (trip == null) return false;

        if (trips.remove(trip)){
            trip.setLocation(null);
            return true;
        }

        return false;
    }
}
