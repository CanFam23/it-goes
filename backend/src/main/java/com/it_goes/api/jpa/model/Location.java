package com.it_goes.api.jpa.model;

import java.util.Collections;
import java.util.HashSet;
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
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

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

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point location;

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

    /**
     * Sets the location (Lat / Long) coordinates of the location
     * @param location {@link org.locationtech.jts.geom.Point} object, containing the lat / long of the location
     * @return {@code true} if the location was successfully added, {@code false} if not.
     * Setting the location returns {@code false} the given {@link org.locationtech.jts.geom.Point} object is null.
     */
    public boolean setLocation(Point location) {
        if (location == null) return false;

        this.location = location;

        return true;
    }

    /**
     * Sets the location (Lat / Long) coordinates of the location
     * @param lat Latitude of location
     * @param lon Longitude of location
     * @return {@code true} if the location was successfully added, {@code false} if not.
     * Setting the location returns {@code false} if converting the given {@code lat} and {@code long}
     * to a {@link org.locationtech.jts.geom.Point} object fails.
     */
    public boolean setLocation(double lat, double lon) {
        try {
            final GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);

            this.location = gf.createPoint(new Coordinate(lon, lat));
            this.location.setSRID(4326);
        }catch (Exception e) {
            return false;
        }

        return true;
    }
}
