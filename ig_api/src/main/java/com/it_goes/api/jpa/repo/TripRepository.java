package com.it_goes.api.jpa.repo;

import com.it_goes.api.jpa.model.Trip;
import com.it_goes.api.jpa.projection.GeoJsonDistanceElevation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface TripRepository extends JpaRepository<Trip, Long> {
    Page<Trip> findAllByOrderByDateOfTripDesc(Pageable pageable);

    /**
     * Given a trip id, builds a json object with the route data for the given trip. If we were to use the data from
     * {@link Trip#route}, it would only contain the {@code x} and {@code y} coordinates. This method gets the {@code x},
     * {@code y}, {@code z} and {@code m} (Which is time} coordinates / measurements and puts them into an array. The JSON is returned
     * as a string with 3 keys:
     * <ul>
     *     <li>id: id of trip</li>
     *     <li>type: type of geometry (Will always be LineString in this case)</li>
     *     <li>coordinates: The coordinates of the linestring, in order of point ascending</li>
     * </ul>
     * Example of what the coordinates would look like:
     * <pre>
     *     {@code
     *     [[-111.385719,45.309258,2448.25,1748713265], // x, y, z, m
     *  [-111.385712,45.309067,2448.644043,1748713296],...]
     *     }
     * </pre>
     * @param tripId ID of trip to search for
     * @return A string of json data with the keys and values mentioned above.
     */
    @Query(value = """
            SELECT json_build_object(
                'id', trip.id,
                'type', 'LineString',
                'coordinates', json_agg(
                    ARRAY[
                        ST_X(pt.geom),       -- X
                        ST_Y(pt.geom),       -- Y
                        ST_Z(pt.geom),       -- Z (elevation), NULL if absent
                        ST_M(pt.geom)        -- M (measure), NULL if absent
                    ]
                    ORDER BY pt.path
                )
            ) AS geojson
            FROM trip
            CROSS JOIN LATERAL ST_DumpPoints(route) AS pt
            WHERE id = :tripId
            GROUP BY trip.id;
            """, nativeQuery = true)
    Optional<String> getTripRoute(@Param("tripId") long tripId);

    /**
     * Gets the route data for all trips in the database, and returns a set of strings of geojson data where each string
     * is the data for one trip.
     * <ul>
     *     <li>id: id of trip</li>
     *     <li>type: type of geometry (Will always be LineString in this case)</li>
     *     <li>coordinates: The coordinates of the linestring, in order of point ascending</li>
     * </ul>
     * @return A set of strings of json data with the keys and values mentioned above.
     */
    @Query(value = """
            SELECT json_build_object(
                'id', trip.id,
                'type', 'LineString',
                'coordinates', json_agg(
                    ARRAY[
                        ST_X(pt.geom),       -- X
                        ST_Y(pt.geom),       -- Y
                        ST_Z(pt.geom),       -- Z (elevation), NULL if absent
                        ST_M(pt.geom)        -- M (measure), NULL if absent
                    ]
                    ORDER BY pt.path
                )
            ) AS geojson,
            (ST_ZMax(trip.route)-ST_ZMin(trip.route)) * 3.280839895 AS elevation_ft,
            ST_Length(trip.route::geography) * 0.0006213712 AS length_mi
            FROM trip
            CROSS JOIN LATERAL ST_DumpPoints(route) AS pt
            GROUP BY trip.id;
            """, nativeQuery = true)
    Set<GeoJsonDistanceElevation> getAllTripRoutes();
}
